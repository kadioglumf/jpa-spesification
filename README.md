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
  "sorts": [
    {
      "field": "roleName",
      "order": "asc"
    },
    {
      "field": "id",
      "order": "desc"
    }
  ],
  "filters": [
    {
      "field": "roleName",
      "type": "text",
      "value": "Admin"
    },
    {
      "field": "active",
      "type": "boolean",
      "value": true
    }
  ]
}
```

Supported request `type` values are `number`, `text`, `boolean`, and `date`. The request `type` is optional; a missing type is treated as `text`.
Use `sorts` for multi-field sorting. Each sort item contains an API `field` and `order` (`asc` or `desc`). Blank sort order defaults to ascending. If `sorts` is missing or empty, legacy `sortField`/`sortOrder` is still accepted. If no request sort is provided, no sorting is applied.

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
Fields are filterable and sortable by default. Override `filterable()` or `sortable()` only when a field should be blocked for that operation.

```java
public enum RoleSearchField implements SearchFieldDefinition {
	  ROLE_NAME(
	      "roleName",
	      "nameTr",
	      Set.of(SearchOperator.CONTAINS)),

	  ACTIVE(
	      "active",
	      "active",
	      SearchFieldType.BOOLEAN,
	      Set.of(SearchOperator.EQUAL, SearchOperator.IN)),

	  MODULE_NAME(
	      "moduleName",
	      "module.nameTr",
	      Set.of(SearchOperator.CONTAINS),
	      List.of(SearchJoinDefinition.left("module")));

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
        new SearchOptions(20, 250, 30, 200, true, true),
        RoleSearchField.values());
```

Pagination still comes from Takeoff UI request fields: `currentPage` and `rowsPerPage`.
Sorting comes from `sorts` when provided. `SearchOptions` only defines safety limits and text matching behavior such as `maxPageSize` and `ignoreBlankTextFilters`.

Optional fetch plan for list responses:

```java
public static final SearchFieldRegistry INSTANCE =
    SearchFieldRegistry.of(
        SearchOptions.defaults(),
        List.of(SearchFetchDefinition.left("module"), SearchFetchDefinition.left("level")),
        RoleSearchField.values());
```

`SearchJoinDefinition` is for filtering/sorting paths. It does not initialize lazy relations.
`SearchFetchDefinition` is for the read model you are returning. Use it when your DTO mapping reads
relations such as `role.getModule().getNameTr()` and you want to avoid N+1 selects.

Fetches are applied only to the content query, not the count query. Prefer fetches for `ManyToOne`
and `OneToOne` relations in pageable list screens. Be careful with collection fetches in paginated
queries; they can duplicate root rows and may require `SearchFetchDefinition.leftDistinct(...)`.

## Filter Type Mapping

- missing/blank type -> `text` -> `CONTAINS`
- `text` -> `CONTAINS`
- `number` -> `EQUAL` for a single value, `IN` for a list
- `boolean` -> `EQUAL`
- `date` -> `EQUAL` for a single value, `BETWEEN` for a range map, `IN` for a list

Old Takeoff UI-control type names such as `checkbox`, `radio`, `datepicker`, and `treeview` are not aliases. They are rejected as unsupported filter types.

## Date Values

Single date:

```json
{ "field": "createdAt", "type": "date", "value": "2026-06-12" }
```

Range:

```json
{
  "field": "createdAt",
  "type": "date",
  "value": {
    "from": "2026-06-01",
    "to": "2026-06-12"
  }
}
```

The converter also accepts range keys like `start/end`, `min/max`, `begin/finish`, and `startDate/endDate`.

Date list:

```json
{ "field": "createdAt", "type": "date", "value": ["2026-06-01", "2026-06-12"] }
```

Date lists map to `IN`; only range maps map to `BETWEEN`.

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
- Consuming services are responsible for database index design.

## Build

```bash
mvn clean package
```
