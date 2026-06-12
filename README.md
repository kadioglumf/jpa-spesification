# Takeoff JPA Specification

Reusable Java 21 / Spring Data JPA library for building safe `Specification` and `Pageable` objects from Takeoff UI Table server-side requests.

This project is no longer a demo Spring Boot application. It does not contain entities, repositories, controllers, application configuration, migrations, Docker files, or business services. Add it as a Maven dependency to a Spring Boot service and define that service's own searchable field registry.

## Maven Dependency

After publishing this artifact to your internal Maven repository:

```xml
<dependency>
  <groupId>com.kadioglumf</groupId>
  <artifactId>takeoff-jpa-specification</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The library includes Spring Boot auto-configuration. In a Spring Boot 3.x service, `SearchSpecificationBuilder` and `SearchPageableFactory` are available as beans after the dependency is added.

## Takeoff Request

The library supports Takeoff UI Table's server-side request shape:

```json
{
  "currentPage": 1,
  "rowsPerPage": 10,
  "sortField": "roleName",
  "sortOrder": "asc",
  "filters": [
    {
      "field": "roleName",
      "type": "text",
      "value": "Admin"
    },
    {
      "field": "active",
      "type": "checkbox",
      "value": ["true"]
    }
  ]
}
```

Takeoff UI `filterType` values are `text`, `checkbox`, `radio`, `datepicker`, and `treeview`. The request `type` is optional; a missing type is treated as `text`.

## Takeoff Response

```json
{
  "data": [],
  "totalItem": 0,
  "currentPage": 1,
  "rowsPerPage": 10,
  "totalPages": 0
}
```

`currentPage` is 1-based in Takeoff UI. Spring `PageRequest` is 0-based, so `currentPage = 1` becomes `PageRequest.of(0, rowsPerPage)`.

## Service Usage

```java
@Service
@RequiredArgsConstructor
public class RoleQueryService {

  private final RoleRepository roleRepository;
  private final SearchSpecificationBuilder searchSpecificationBuilder;
  private final SearchPageableFactory searchPageableFactory;

  public TakeoffTableResponse<RoleResponse> search(TakeoffTableRequest request) {
    Specification<Role> specification =
        searchSpecificationBuilder.build(request, RoleSearchRegistry.INSTANCE);

    Pageable pageable =
        searchPageableFactory.create(request, RoleSearchRegistry.INSTANCE);

    Page<RoleResponse> page = roleRepository.findAll(specification, pageable).map(this::toResponse);
    return TakeoffTableResponse.from(page);
  }
}
```

Repository:

```java
public interface RoleRepository
    extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {}
```

## Field Registry

Each consuming service owns its registry. The frontend sends API field names only; the service maps those names to entity paths, field types, allowed operators, joins, and sort/filter permissions.

```java
public enum RoleSearchField implements SearchFieldDefinition {
  ROLE_NAME(
      "roleName",
      "nameTr",
      SearchFieldType.STRING,
      Set.of(SearchOperator.CONTAINS, SearchOperator.STARTS_WITH, SearchOperator.EQUAL),
      true,
      true),

  ACTIVE(
      "active",
      "active",
      SearchFieldType.BOOLEAN,
      Set.of(SearchOperator.EQUAL, SearchOperator.IN),
      true,
      true),

  MODULE_NAME(
      "moduleName",
      "module.nameTr",
      SearchFieldType.STRING,
      Set.of(SearchOperator.CONTAINS, SearchOperator.STARTS_WITH, SearchOperator.EQUAL),
      true,
      true,
      List.of(SearchJoinDefinition.left("module")),
      false);

  // implement SearchFieldDefinition methods or use your own class/builder
}
```

```java
public final class RoleSearchRegistry {
  public static final SearchFieldRegistry INSTANCE =
      SearchFieldRegistry.of(SearchOptions.defaults(), RoleSearchField.values());

  private RoleSearchRegistry() {}
}
```

Custom options:

```java
public static final SearchFieldRegistry INSTANCE =
    SearchFieldRegistry.of(
        SearchOptions.builder()
            .maxPageSize(250)
            .maxFilters(30)
            .maxInValues(200)
            .defaultSortField("id")
            .defaultSortDirection(Sort.Direction.DESC)
            .build(),
        RoleSearchField.values());
```

Pagination still comes from Takeoff UI request fields: `currentPage` and `rowsPerPage`.
`SearchOptions` only defines safety limits and defaults such as `maxPageSize` and default sorting.

## Filter Type Mapping

- missing/blank type -> `text` -> `CONTAINS`
- `text` -> `CONTAINS`
- `checkbox` -> `IN`, or `EQUAL` for a single boolean checkbox value
- `radio` -> `EQUAL`
- `datepicker` -> `EQUAL` or `BETWEEN` depending on value shape
- `treeview` -> `IN`

`treeview` and `checkbox` both map to `IN`, but they represent different Takeoff UI controls. `treeview` values should still be primitive selected keys/codes, not full tree node objects.

## Datepicker Values

Single date:

```json
{ "field": "createdAt", "type": "datepicker", "value": "2026-06-12" }
```

Range:

```json
{
  "field": "createdAt",
  "type": "datepicker",
  "value": {
    "from": "2026-06-01",
    "to": "2026-06-12"
  }
}
```

The converter also accepts range keys like `start/end`, `min/max`, `begin/finish`, and `startDate/endDate`.

## Supported Types And Operators

Field types: `STRING`, `LONG`, `INTEGER`, `BIG_DECIMAL`, `BOOLEAN`, `LOCAL_DATE`, `LOCAL_DATE_TIME`, `OFFSET_DATE_TIME`, `INSTANT`, `ENUM`.

Internal operators: `EQUAL`, `NOT_EQUAL`, `IN`, `NOT_IN`, `BETWEEN`, `GREATER_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN`, `LESS_THAN_OR_EQUAL`, `STARTS_WITH`, `ENDS_WITH`, `CONTAINS`, `IS_NULL`, `IS_NOT_NULL`.

Clients do not send these operators. The backend selects the operator from Takeoff filter type and registry definitions.

## Security Notes

- Client fields are never passed directly to `root.get()`.
- Unknown fields are rejected.
- Non-filterable and non-sortable fields are rejected.
- Unsupported Takeoff filter types and unsupported operators are rejected.
- Error messages avoid entity paths, join paths, SQL, and Criteria internals.
- Raw filter values are not logged by this library.

## Index Notes

The library does not create indexes or migrations.

- `EQUAL` and range filters can use normal database indexes when the consuming service creates them.
- Case-insensitive text search using `upper(column)` may require expression indexes.
- `CONTAINS` with `%value%` usually needs a PostgreSQL trigram index on large tables.
- `STARTS_WITH` is more index-friendly than `CONTAINS`.
- Consuming services are responsible for database index design.

## Build

```bash
mvn clean package
```
