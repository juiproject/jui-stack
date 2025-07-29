# Remoting Patterns and Design Guide

This document provides a comprehensive reference for implementing remoting and RPC patterns using the JUI framework. It combines concepts, best practices, design patterns, and practical examples for both developers and AI systems building remoting solutions.

## Table of Contents

1. [Core Concepts](#core-concepts)
2. [Architecture Overview](#architecture-overview)
3. [Base Classes and Framework](#base-classes-and-framework)
4. [Standard Entity Patterns](#standard-entity-patterns)
5. [Query Patterns](#query-patterns)
6. [Command Patterns](#command-patterns)
7. [Processor Implementation](#processor-implementation)
8. [Best Practices](#best-practices)
9. [Complete Examples](#complete-examples)
10. [Reference Implementation](#reference-implementation)

## Core Concepts

### Remoting Overview

JUI remoting provides a type-safe, pattern-based approach to client-server communication that:

- **Abstracts transport mechanisms** - Works with RPC, REST, or other protocols
- **Enforces strict typing** - Compile-time safety between client and server
- **Supports polymorphism** - Clean inheritance hierarchies in transfer objects
- **Enables command composition** - Multiple operations in atomic transactions
- **Provides automatic serialization** - Handles complex object graphs seamlessly

### Key Components

1. **Commands** (`ICommand`) - Operations that modify server state
2. **Queries** (`Query<T>`) - Operations that retrieve data without side effects
3. **Results** (`Result`) - Data transfer objects returned from queries
4. **References** (`Ref`) - Type-safe entity identifiers and lookup mechanisms
5. **Processors** - Server-side handlers that execute commands and queries
6. **Executor** - Coordinates processor dispatch and transaction management

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Side   │    │   Transport     │    │   Server Side   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Service Handler │    │ RPC Transport   │    │ RPC Endpoint    │
│ Query/Command   │────│ Serialization   │────│ Executor        │
│ Result Objects  │    │ Error Handling  │    │ Processors      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Processing Flow

```
Client Request → Transport → Service → Executor → Processor → Business Logic
                                                      ↓
Client Response ← Transport ← Service ← Executor ← Result ← Entity/Data
```

## Base Classes and Framework

### Command Base Class (`C`)

All commands extend the `C` class which provides:

```java
public abstract class MyCommand extends C {
    // Constructor patterns
    protected MyCommand() {}                    // For serialization
    public MyCommand(MyRef ref) {}              // Update existing entity
    public MyCommand(MyConstruct construct) {}  // Create new entity
    public MyCommand(ICommand reference) {}     // Reference another command

    // Value objects for tracking changes
    private VString title = new VString();
    private VLong categoryId = new VLong();
    
    // Fluent assignment methods
    public MyCommand title(String title) {
        assign(this.title, title);
        return this;
    }
    
    // Getters/setters for serialization
    public VString getTitle() { return title; }
    public void setTitle(VString title) { this.title = title; }
}
```

### Query Base Class (`Query<T>`)

Queries are parameterized with their return type:

```java
public class MyLookup extends Query<MyLookupResult> {
    // Query-specific fields
    private boolean includeDetails = false;
    
    // Fluent configuration
    public MyLookup includeDetails(boolean include) {
        this.includeDetails = include;
        return this;
    }
}
```

### Result Base Class (`Result`)

All transfer objects should extend Result:

```java
public class MyResult extends Result {
    private String title;
    private Long categoryId;
    private LocalDateTime created;
    
    // Standard getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
```

### Reference Base Class (`Ref`)

References provide type-safe entity identification:

```java
@JsonSerializable(settersRequired = false, type = TypeMode.SIMPLE)
public abstract class MyRef extends Ref {
    
    // Static factory methods
    public static MyRefById byId(long id) {
        return With.$(new MyRefById(), ref -> ref.setId(id));
    }
    
    public static MyRefByEmail byEmail(String email) {
        return With.$(new MyRefByEmail(), ref -> ref.setEmail(email));
    }
    
    public static MyRefByContext byContext() {
        return new MyRefByContext();
    }
    
    // Concrete reference implementations
    public static class MyRefById extends MyRef {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
    
    public static class MyRefByEmail extends MyRef {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class MyRefByContext extends MyRef {
        // References current user context
    }
}
```

## Standard Entity Patterns

For any business entity `Entity`, implement these standard patterns:

### 1. Reference Hierarchy

```java
// Base reference class
@JsonSerializable(settersRequired = false, type = TypeMode.SIMPLE)
public abstract class EntityRef extends Ref {
    public static EntityRefById byId(long id) { ... }
    public static EntityRefByCode byCode(String code) { ... }
    // Additional reference types as needed
}
```

### 2. Transfer Object Hierarchy

```java
// Base result for shared properties
public class EntityResult extends Result {
    private Long id;
    private String title;
    private LocalDateTime created;
    // Common properties
}

// Detailed result for lookups
public class EntityLookupResult extends EntityResult {
    private String description;
    private List<CategoryResult> categories;
    // Detailed properties
}

// Lightweight result for queries/lists
public class EntityQueryResult extends EntityResult {
    private String summary;
    // Minimal properties for listing
}
```

### 3. Query Classes

```java
// Single entity lookup
public class EntityLookup extends Lookup<EntityLookupResult, EntityRef> {
    public static EntityLookup byId(long id) {
        return new EntityLookup(EntityRef.byId(id));
    }
    
    // Configuration options
    private boolean includeCategories = false;
    
    public EntityLookup includeCategories(boolean include) {
        this.includeCategories = include;
        return this;
    }
}

// Search/collection query
public class EntityQuery extends PageQuery<EntityQueryResultSet> {
    private String keywords;
    private Sort sort = Sort.TITLE_ASC;
    
    public enum Sort {
        TITLE_ASC, TITLE_DESC, CREATED_ASC, CREATED_DESC
    }
    
    public EntityQuery(int page, int pageSize) {
        super(page, pageSize);
    }
    
    public EntityQuery keywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
    
    @Override
    public boolean filtering() {
        return !StringSupport.empty(keywords) || super.filtering();
    }
}

// Result set wrapper
public class EntityQueryResultSet extends ResultSet<EntityQueryResult> {
    public EntityQueryResultSet() {
        super(null, 0);
    }
    
    public EntityQueryResultSet(Iterable<EntityQueryResult> results, int totalResults) {
        super(results, totalResults);
    }
    
    public <S> EntityQueryResultSet(Iterable<S> results, IConverter<S, EntityQueryResult> converter, int totalResults) {
        super(results, converter, totalResults);
    }
}
```

### 4. Command Class

```java
public class EntityCommand extends C {
    private VString title = new VString();
    private VString description = new VString();
    private VLong categoryId = new VLong();
    
    // Constructors
    protected EntityCommand() {}
    
    public EntityCommand(EntityRef ref) {
        super(ref);
    }
    
    public EntityCommand(EntityConstruct construct) {
        super(construct);
    }
    
    // Fluent assignment methods
    public EntityCommand title(String title) {
        assign(this.title, title);
        return this;
    }
    
    public EntityCommand description(String description) {
        assign(this.description, description);
        return this;
    }
    
    public EntityCommand categoryId(Long categoryId) {
        assign(this.categoryId, categoryId);
        return this;
    }
    
    // Construction helper for new entities
    public static class EntityConstruct extends Construct {
        private String title;
        private String description;
        
        public EntityConstruct(String title, String description) {
            this.title = title;
            this.description = description;
        }
        
        // Getters for serialization
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
    
    // Serialization getters/setters
    public VString getTitle() { return title; }
    public void setTitle(VString title) { this.title = title; }
    
    public VString getDescription() { return description; }
    public void setDescription(VString description) { this.description = description; }
    
    public VLong getCategoryId() { return categoryId; }
    public void setCategoryId(VLong categoryId) { this.categoryId = categoryId; }
}
```

## Query Patterns

### Simple Lookup Pattern

For retrieving a single entity by reference:

```java
// Query class
public class UserLookup extends Lookup<UserLookupResult, UserRef> {
    public static UserLookup byId(long id) {
        return new UserLookup(UserRef.byId(id));
    }
    
    public static UserLookup byEmail(String email) {
        return new UserLookup(UserRef.byEmail(email));
    }
    
    public static UserLookup byContext() {
        return new UserLookup(UserRef.byContext());
    }
}

// Processor
@RPCHandlerProcessor
public class UserLookupProcessor extends QueryProcessor<QueryContext, UserLookupResult, UserLookup> {
    
    public UserLookupProcessor() {
        super(UserLookup.class);
    }
    
    @Override
    protected UserLookupResult process(QueryContext context, UserLookup lookup) throws ProcessorException {
        UserRef ref = lookup.ref();
        UserEntity entity;
        
        if (ref instanceof UserRefById) {
            entity = userRepository.findById(((UserRefById) ref).getId());
        } else if (ref instanceof UserRefByEmail) {
            entity = userRepository.findByEmail(((UserRefByEmail) ref).getEmail());
        } else if (ref instanceof UserRefByContext) {
            entity = context.getCurrentUser();
        } else {
            throw new ProcessorException().add(ErrorType.NOT_FOUND, "Invalid user reference");
        }
        
        if (entity == null) {
            throw new ProcessorException().add(ErrorType.NOT_FOUND, "User not found");
        }
        
        return UserLookupResultConverter.fromEntity().convert(entity);
    }
}
```

### Search/Collection Pattern

For retrieving collections with filtering and pagination:

```java
// Query with filtering and sorting
public class ProductQuery extends PageQuery<ProductQueryResultSet> {
    private String keywords;
    private Long categoryId;
    private ProductStatus status;
    private Sort sort = Sort.NAME_ASC;
    
    public enum Sort {
        NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC, CREATED_ASC, CREATED_DESC
    }
    
    protected ProductQuery() {}
    
    public ProductQuery(int page, int pageSize) {
        super(page, pageSize);
    }
    
    // Fluent configuration
    public ProductQuery keywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
    
    public ProductQuery categoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }
    
    public ProductQuery status(ProductStatus status) {
        this.status = status;
        return this;
    }
    
    public ProductQuery sort(Sort sort) {
        this.sort = sort;
        return this;
    }
    
    @Override
    public boolean filtering() {
        return !StringSupport.empty(keywords) || 
               categoryId != null || 
               status != null || 
               super.filtering();
    }
}

// Processor with repository integration
@RPCHandlerProcessor
public class ProductQueryProcessor extends QueryProcessor<QueryContext, ProductQueryResultSet, ProductQuery> {
    
    @Autowired
    private ProductRepository productRepository;
    
    public ProductQueryProcessor() {
        super(ProductQuery.class);
    }
    
    @Override
    protected ProductQueryResultSet process(QueryContext context, ProductQuery query) throws ProcessorException {
        // Build query criteria
        ProductQueryCriteria criteria = new ProductQueryCriteria();
        criteria.setKeywords(query.getKeywords());
        criteria.setCategoryId(query.getCategoryId());
        criteria.setStatus(query.getStatus());
        criteria.setSort(query.getSort());
        criteria.setPage(query.getPage());
        criteria.setPageSize(query.getPageSize());
        
        // Execute query
        PaginatedQueryResultSet<ProductEntity> results = productRepository.query(criteria);
        
        // Convert and return
        return new ProductQueryResultSet(
            results.getResults(),
            ProductQueryResultConverter.fromEntity(),
            (int) results.getTotalResults()
        );
    }
}
```

### Aggregation/Statistics Pattern

For retrieving computed data and analytics:

```java
// Statistics query
public class SalesStatsQuery extends Query<SalesStatsResult> {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long categoryId;
    private StatsGrouping grouping = StatsGrouping.DAILY;
    
    public enum StatsGrouping {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    public SalesStatsQuery(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Configuration methods
    public SalesStatsQuery categoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }
    
    public SalesStatsQuery grouping(StatsGrouping grouping) {
        this.grouping = grouping;
        return this;
    }
}

// Complex result with multiple data points
public class SalesStatsResult extends Result {
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private BigDecimal averageOrderValue;
    private List<SalesDataPoint> dataPoints;
    
    public static class SalesDataPoint extends Result {
        private LocalDate date;
        private BigDecimal revenue;
        private Integer orders;
        // getters/setters
    }
}
```

## Command Patterns

### CRUD Command Pattern

Standard create, update, delete operations:

```java
public class OrderCommand extends C {
    // Value objects for change tracking
    private VString customerEmail = new VString();
    private VBigDecimal totalAmount = new VBigDecimal();
    private VLocalDateTime deliveryDate = new VLocalDateTime();
    private VList<OrderItemCommand> items = new VList<>();
    
    // Constructors for different operations
    protected OrderCommand() {}
    
    // Update existing order
    public OrderCommand(OrderRef ref) {
        super(ref);
    }
    
    // Create new order
    public OrderCommand(OrderConstruct construct) {
        super(construct);
    }
    
    // Fluent modification methods
    public OrderCommand customerEmail(String email) {
        assign(this.customerEmail, email);
        return this;
    }
    
    public OrderCommand totalAmount(BigDecimal amount) {
        assign(this.totalAmount, amount);
        return this;
    }
    
    public OrderCommand addItem(OrderItemCommand item) {
        if (this.items.value() == null) {
            assign(this.items, new ArrayList<>());
        }
        this.items.value().add(item);
        this.items.markSet();
        return this;
    }
    
    // Construction data for new entities
    public static class OrderConstruct extends Construct {
        private String customerEmail;
        private List<OrderItemData> items;
        
        public OrderConstruct(String customerEmail, List<OrderItemData> items) {
            this.customerEmail = customerEmail;
            this.items = items;
        }
        
        // getters for serialization
    }
}
```

### Composite Command Pattern

Commands that operate on multiple related entities:

```java
public class InvoiceCommand extends C {
    private VString invoiceNumber = new VString();
    private VLocalDate dueDate = new VLocalDate();
    
    // Child commands for line items
    private List<InvoiceLineCommand> lineItems = new ArrayList<>();
    
    public InvoiceCommand addLineItem(ProductRef product, Integer quantity, BigDecimal unitPrice) {
        InvoiceLineCommand lineItem = new InvoiceLineCommand(this.reference())
            .productId(product.getId())
            .quantity(quantity)
            .unitPrice(unitPrice);
        lineItems.add(lineItem);
        return this;
    }
    
    // Method to get all commands for processing
    public List<ICommand> getAllCommands() {
        List<ICommand> commands = new ArrayList<>();
        commands.add(this);
        commands.addAll(lineItems);
        return commands;
    }
}

// Child command that references parent
public class InvoiceLineCommand extends C {
    private VLong productId = new VLong();
    private VInteger quantity = new VInteger();
    private VBigDecimal unitPrice = new VBigDecimal();
    
    public InvoiceLineCommand(ReferenceLookup invoiceRef) {
        super(invoiceRef); // References parent invoice command
    }
}
```

### Batch Operation Pattern

For operations on multiple entities:

```java
public class BulkProductUpdateCommand extends C {
    private List<Long> productIds;
    private VBigDecimal priceAdjustment = new VBigDecimal();
    private VLong newCategoryId = new VLong();
    private VBoolean active = new VBoolean();
    
    public BulkProductUpdateCommand(List<Long> productIds) {
        this.productIds = productIds;
    }
    
    public BulkProductUpdateCommand adjustPrices(BigDecimal adjustment) {
        assign(this.priceAdjustment, adjustment);
        return this;
    }
    
    public BulkProductUpdateCommand moveToCategory(Long categoryId) {
        assign(this.newCategoryId, categoryId);
        return this;
    }
    
    public BulkProductUpdateCommand setActive(boolean active) {
        assign(this.active, active);
        return this;
    }
}
```

## Processor Implementation

### Query Processor Pattern

```java
@RPCHandlerProcessor
public class EntityLookupProcessor extends QueryProcessor<QueryContext, EntityLookupResult, EntityLookup> {
    
    @Autowired
    private EntityRepository entityRepository;
    
    public EntityLookupProcessor() {
        super(EntityLookup.class);
    }
    
    @Override
    protected EntityLookupResult process(QueryContext context, EntityLookup lookup) throws ProcessorException {
        // Resolve entity from reference
        EntityRef ref = lookup.ref();
        EntityModel entity = resolveEntity(ref, context);
        
        // Check permissions
        if (!hasReadPermission(entity, context)) {
            throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "Access denied");
        }
        
        // Convert to result
        return EntityLookupResultConverter.fromEntity(lookup.isIncludeDetails()).convert(entity);
    }
    
    private EntityModel resolveEntity(EntityRef ref, QueryContext context) throws ProcessorException {
        if (ref instanceof EntityRefById) {
            return entityRepository.findById(((EntityRefById) ref).getId())
                .orElseThrow(() -> new ProcessorException().add(ErrorType.NOT_FOUND, "Entity not found"));
        }
        // Handle other reference types...
        throw new ProcessorException().add(ErrorType.INVALID_REQUEST, "Invalid reference type");
    }
}
```

### Command Processor Pattern

```java
@RPCHandlerProcessor
public class EntityCommandProcessor extends CRUDCommandProcessor<EntityCommand, EntityModel> {
    
    @Autowired
    private EntityRepository entityRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public EntityCommandProcessor() {
        super(EntityCommand.class, EntityModel.class);
    }
    
    @Override
    protected EntityModel create(EntityCommand command, CommandContext context) throws ProcessorException {
        EntityConstruct construct = command.construct();
        
        // Validate construction data
        if (StringSupport.empty(construct.getTitle())) {
            throw new ProcessorException().add(ErrorType.VALIDATION, "title", "Title is required");
        }
        
        // Create new entity
        EntityModel entity = new EntityModel();
        entity.setTitle(construct.getTitle());
        entity.setDescription(construct.getDescription());
        entity.setCreatedBy(context.getCurrentUser());
        entity.setCreated(LocalDateTime.now());
        
        return entityRepository.save(entity);
    }
    
    @Override
    protected EntityModel resolve(EntityCommand command, CommandContext context) throws ProcessorException {
        EntityRef ref = command.lookup();
        EntityModel entity = resolveEntity(ref, context);
        
        // Check permissions
        if (!hasWritePermission(entity, context)) {
            throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "Access denied");
        }
        
        return entity;
    }
    
    @Override
    protected void modify(EntityModel entity, EntityCommand command, CommandContext context, Modification modification) throws ProcessorException {
        boolean canModify = hasWritePermission(entity, context);
        
        // Update title if provided
        modification.updater(entity::setTitle)
            .accessCheck(canModify)
            .currentValue(entity.getTitle())
            .path("title")
            .updateByCmd(command.getTitle());
        
        // Update description if provided
        modification.updater(entity::setDescription)
            .accessCheck(canModify)
            .currentValue(entity.getDescription())
            .path("description")
            .updateByCmd(command.getDescription());
        
        // Update category with validation
        modification.updater(entity::setCategory)
            .accessCheck(canModify)
            .currentValue(entity.getCategory())
            .path("categoryId")
            .updateByCmd(command.getCategoryId(), id -> {
                if (id == null) return null;
                return categoryRepository.findById(id)
                    .orElseThrow(() -> new ValidationException().add(new Message("Category not found")));
            });
    }
    
    @Override
    protected void delete(EntityModel entity, CommandContext context) throws ProcessorException {
        if (!hasDeletePermission(entity, context)) {
            throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "Access denied");
        }
        
        entityRepository.delete(entity);
    }
}
```

### Converter Pattern

For clean entity-to-DTO conversion:

```java
public class EntityLookupResultConverter {
    
    public static IConverter<EntityModel, EntityLookupResult> fromEntity() {
        return fromEntity(false);
    }
    
    public static IConverter<EntityModel, EntityLookupResult> fromEntity(boolean includeDetails) {
        return IConverter.create(EntityLookupResult.class, (entity, dto) -> {
            // Basic properties
            set(dto::setId, entity.getId());
            set(dto::setTitle, entity.getTitle());
            set(dto::setDescription, entity.getDescription());
            set(dto::setCreated, entity.getCreated());
            
            // Related entities
            set(dto::setCategory, entity.getCategory(), new CategoryResult(), (v, s) -> {
                set(v::setId, s.getId());
                set(v::setName, s.getName());
            });
            
            // Conditional details
            if (includeDetails) {
                set(dto::setDetailedDescription, entity.getDetailedDescription());
                
                // Related collections
                entity.getTags().forEach(tag -> {
                    add(dto.getTags(), new TagResult(), v -> {
                        set(v::setId, tag.getId());
                        set(v::setName, tag.getName());
                    });
                });
            }
        });
    }
}
```

## Best Practices

### Naming Conventions

Follow consistent naming patterns:

```java
// Entity: Product
// Base classes
ProductRef                  // Reference hierarchy
ProductResult              // Base result class

// Specific classes  
ProductLookupResult        // Detailed single entity
ProductLookup              // Single entity query
ProductQueryResult         // Lightweight list item
ProductQuery               // Collection query
ProductQueryResultSet      // Collection result wrapper
ProductCommand             // Modification command

// Processors
ProductLookupProcessor     // Single entity processor
ProductQueryProcessor      // Collection processor  
ProductCommandProcessor    // Modification processor

// Converters
ProductLookupResultConverter
ProductQueryResultConverter
```

### Value Object Usage

Use `V<T>` classes for tracking changes:

```java
public class UserCommand extends C {
    private VString email = new VString();
    private VString firstName = new VString();
    private VString lastName = new VString();
    private VBoolean active = new VBoolean();
    
    // Fluent assignment with null support
    public UserCommand email(String email) {
        assign(this.email, email);  // Can assign null
        return this;
    }
    
    // Check if value was explicitly set
    public boolean hasEmailChange() {
        return email.isSet();
    }
}
```

### Error Handling

Provide meaningful error messages:

```java
@Override
protected UserLookupResult process(QueryContext context, UserLookup lookup) throws ProcessorException {
    UserRef ref = lookup.ref();
    
    if (ref instanceof UserRefById) {
        Long id = ((UserRefById) ref).getId();
        if (id == null || id <= 0) {
            throw new ProcessorException()
                .add(ErrorType.VALIDATION, "id", "Valid user ID is required");
        }
        
        UserEntity user = userRepository.findById(id);
        if (user == null) {
            throw new ProcessorException()
                .add(ErrorType.NOT_FOUND, "User with ID " + id + " not found");
        }
        
        return UserLookupResultConverter.fromEntity().convert(user);
    }
    
    throw new ProcessorException()
        .add(ErrorType.INVALID_REQUEST, "Unsupported reference type");
}
```

### Security and Authorization

Implement consistent security checks:

```java
public abstract class SecureQueryProcessor<T, Q extends Query<T>> extends QueryProcessor<QueryContext, T, Q> {
    
    protected SecureQueryProcessor(Class<Q> queryClass) {
        super(queryClass);
    }
    
    @Override
    public final T process(QueryContext context, Object query) throws ProcessorException {
        checkAuthentication(context);
        
        @SuppressWarnings("unchecked")
        Q typedQuery = (Q) query;
        
        checkQueryPermissions(context, typedQuery);
        
        return processSecure(context, typedQuery);
    }
    
    protected abstract T processSecure(QueryContext context, Q query) throws ProcessorException;
    
    private void checkAuthentication(QueryContext context) throws ProcessorException {
        if (context.getCurrentUser() == null) {
            throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "Authentication required");
        }
    }
    
    protected void checkQueryPermissions(QueryContext context, Q query) throws ProcessorException {
        // Override in subclasses for specific permission checks
    }
}
```

### Client-Side Usage Patterns

```java
// Simple lookup
UserLookup.byId(userId)
    .executeWith(new AppServiceHandler<UserLookupResult>()
        .onSuccessful(user -> {
            // Handle successful lookup
            displayUser(user);
        })
        .onFailure((errors, status) -> {
            // Handle error
            showError("Failed to load user: " + errors.get(0).getMessage());
        }));

// Search with filtering
UserQuery query = new UserQuery(0, 20)
    .keywords("john")
    .department("Engineering")
    .active(true);
    
query.executeWith(new AppServiceHandler<UserQueryResultSet>()
    .onSuccessful(results -> {
        updateUserList(results.getResults());
        updatePagination(results.getTotalResults());
    }));

// Command execution
UserCommand command = new UserCommand(UserRef.byId(userId))
    .firstName("John")
    .lastName("Doe")
    .email("john.doe@example.com");
    
command.executeWith(new AppServiceHandler<UserLookupResult>()
    .onSuccessful(updatedUser -> {
        showMessage("User updated successfully");
        refreshUserDisplay(updatedUser);
    })
    .onFailure((errors, status) -> {
        // Display validation errors
        showValidationErrors(errors);
    }));
```

## Complete Examples

### Complete User Management Example

This example shows a complete implementation for user management:

#### Transfer Classes

```java
// Reference hierarchy
@JsonSerializable(settersRequired = false, type = TypeMode.SIMPLE)
public abstract class UserRef extends Ref {
    public static UserRefById byId(long id) {
        return With.$(new UserRefById(), ref -> ref.setId(id));
    }
    
    public static UserRefByEmail byEmail(String email) {
        return With.$(new UserRefByEmail(), ref -> ref.setEmail(email));
    }
    
    public static UserRefByContext byContext() {
        return new UserRefByContext();
    }
    
    public static class UserRefById extends UserRef {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
    
    public static class UserRefByEmail extends UserRef {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class UserRefByContext extends UserRef {}
}

// Base result
public class UserResult extends Result {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean active;
    private LocalDateTime created;
    
    // getters and setters
}

// Detailed lookup result
public class UserLookupResult extends UserResult {
    private String phone;
    private String department;
    private UserProfileResult profile;
    private List<RoleResult> roles;
    
    // additional getters and setters
}

// Lightweight query result
public class UserQueryResult extends UserResult {
    private String departmentName;
    private String lastLoginDisplay;
    
    // minimal additional properties
}

// Collection wrapper
public class UserQueryResultSet extends ResultSet<UserQueryResult> {
    public UserQueryResultSet() {
        super(null, 0);
    }
    
    public UserQueryResultSet(Iterable<UserQueryResult> results, int totalResults) {
        super(results, totalResults);
    }
    
    public <S> UserQueryResultSet(Iterable<S> results, IConverter<S, UserQueryResult> converter, int totalResults) {
        super(results, converter, totalResults);
    }
}

// Lookup query
public class UserLookup extends Lookup<UserLookupResult, UserRef> {
    private boolean includeProfile = false;
    private boolean includeRoles = false;
    
    protected UserLookup() {}
    
    public UserLookup(UserRef ref) {
        super(ref);
    }
    
    public static UserLookup byId(long id) {
        return new UserLookup(UserRef.byId(id));
    }
    
    public UserLookup includeProfile(boolean include) {
        this.includeProfile = include;
        return this;
    }
    
    public UserLookup includeRoles(boolean include) {
        this.includeRoles = include;
        return this;
    }
}

// Search query
public class UserQuery extends PageQuery<UserQueryResultSet> {
    private String keywords;
    private String department;
    private Boolean active;
    private Sort sort = Sort.LAST_NAME_ASC;
    
    public enum Sort {
        FIRST_NAME_ASC, FIRST_NAME_DESC,
        LAST_NAME_ASC, LAST_NAME_DESC,
        EMAIL_ASC, EMAIL_DESC,
        CREATED_ASC, CREATED_DESC
    }
    
    protected UserQuery() {}
    
    public UserQuery(int page, int pageSize) {
        super(page, pageSize);
    }
    
    public UserQuery keywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
    
    public UserQuery department(String department) {
        this.department = department;
        return this;
    }
    
    public UserQuery active(Boolean active) {
        this.active = active;
        return this;
    }
    
    public UserQuery sort(Sort sort) {
        this.sort = sort;
        return this;
    }
    
    @Override
    public boolean filtering() {
        return !StringSupport.empty(keywords) || 
               !StringSupport.empty(department) || 
               active != null || 
               super.filtering();
    }
}

// Command
public class UserCommand extends C {
    private VString firstName = new VString();
    private VString lastName = new VString();
    private VString email = new VString();
    private VString phone = new VString();
    private VString department = new VString();
    private VBoolean active = new VBoolean();
    private VList<Long> roleIds = new VList<>();
    
    protected UserCommand() {}
    
    public UserCommand(UserRef ref) {
        super(ref);
    }
    
    public UserCommand(UserConstruct construct) {
        super(construct);
    }
    
    // Fluent methods
    public UserCommand firstName(String firstName) {
        assign(this.firstName, firstName);
        return this;
    }
    
    public UserCommand lastName(String lastName) {
        assign(this.lastName, lastName);
        return this;
    }
    
    public UserCommand email(String email) {
        assign(this.email, email);
        return this;
    }
    
    // Construction data
    public static class UserConstruct extends Construct {
        private String firstName;
        private String lastName;
        private String email;
        
        public UserConstruct(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
        
        // getters
    }
    
    // Serialization getters/setters
    public VString getFirstName() { return firstName; }
    public void setFirstName(VString firstName) { this.firstName = firstName; }
    // ... other getters/setters
}
```

#### Processors

```java
// Lookup processor
@RPCHandlerProcessor
public class UserLookupProcessor extends QueryProcessor<QueryContext, UserLookupResult, UserLookup> {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserLookupProcessor() {
        super(UserLookup.class);
    }
    
    @Override
    protected UserLookupResult process(QueryContext context, UserLookup lookup) throws ProcessorException {
        UserRef ref = lookup.ref();
        UserEntity entity = resolveUser(ref, context);
        
        if (!hasReadPermission(entity, context)) {
            throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "Access denied");
        }
        
        return UserLookupResultConverter
            .fromEntity(lookup.isIncludeProfile(), lookup.isIncludeRoles())
            .convert(entity);
    }
    
    private UserEntity resolveUser(UserRef ref, QueryContext context) throws ProcessorException {
        if (ref instanceof UserRefById) {
            return userRepository.findById(((UserRefById) ref).getId())
                .orElseThrow(() -> new ProcessorException().add(ErrorType.NOT_FOUND, "User not found"));
        } else if (ref instanceof UserRefByEmail) {
            return userRepository.findByEmail(((UserRefByEmail) ref).getEmail())
                .orElseThrow(() -> new ProcessorException().add(ErrorType.NOT_FOUND, "User not found"));
        } else if (ref instanceof UserRefByContext) {
            UserEntity currentUser = context.getCurrentUser();
            if (currentUser == null) {
                throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "No current user");
            }
            return currentUser;
        }
        
        throw new ProcessorException().add(ErrorType.INVALID_REQUEST, "Invalid user reference");
    }
}

// Query processor
@RPCHandlerProcessor
public class UserQueryProcessor extends QueryProcessor<QueryContext, UserQueryResultSet, UserQuery> {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserQueryProcessor() {
        super(UserQuery.class);
    }
    
    @Override
    protected UserQueryResultSet process(QueryContext context, UserQuery query) throws ProcessorException {
        UserQueryCriteria criteria = buildCriteria(query, context);
        PaginatedQueryResultSet<UserEntity> results = userRepository.query(criteria);
        
        return new UserQueryResultSet(
            results.getResults(),
            UserQueryResultConverter.fromEntity(),
            (int) results.getTotalResults()
        );
    }
    
    private UserQueryCriteria buildCriteria(UserQuery query, QueryContext context) {
        UserQueryCriteria criteria = new UserQueryCriteria();
        criteria.setKeywords(query.getKeywords());
        criteria.setDepartment(query.getDepartment());
        criteria.setActive(query.getActive());
        criteria.setSort(query.getSort());
        criteria.setPage(query.getPage());
        criteria.setPageSize(query.getPageSize());
        
        // Apply security filters
        if (!context.getCurrentUser().hasRole("ADMIN")) {
            criteria.setVisibleToDepartment(context.getCurrentUser().getDepartment());
        }
        
        return criteria;
    }
}

// Command processor
@RPCHandlerProcessor
public class UserCommandProcessor extends CRUDCommandProcessor<UserCommand, UserEntity> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    public UserCommandProcessor() {
        super(UserCommand.class, UserEntity.class);
    }
    
    @Override
    protected UserEntity create(UserCommand command, CommandContext context) throws ProcessorException {
        UserConstruct construct = command.construct();
        
        // Validation
        validateEmailUnique(construct.getEmail(), null);
        
        UserEntity entity = new UserEntity();
        entity.setFirstName(construct.getFirstName());
        entity.setLastName(construct.getLastName());
        entity.setEmail(construct.getEmail());
        entity.setCreatedBy(context.getCurrentUser());
        entity.setCreated(LocalDateTime.now());
        entity.setActive(true);
        
        return userRepository.save(entity);
    }
    
    @Override
    protected UserEntity resolve(UserCommand command, CommandContext context) throws ProcessorException {
        UserRef ref = command.lookup();
        UserEntity entity = resolveUser(ref, context);
        
        if (!hasWritePermission(entity, context)) {
            throw new ProcessorException().add(ErrorType.ACCESS_DENIED, "Access denied");
        }
        
        return entity;
    }
    
    @Override
    protected void modify(UserEntity entity, UserCommand command, CommandContext context, Modification modification) 
            throws ProcessorException {
        
        boolean canModify = hasWritePermission(entity, context);
        
        modification.updater(entity::setFirstName)
            .accessCheck(canModify)
            .currentValue(entity.getFirstName())
            .path("firstName")
            .updateByCmd(command.getFirstName());
        
        modification.updater(entity::setLastName)
            .accessCheck(canModify)
            .currentValue(entity.getLastName())
            .path("lastName")
            .updateByCmd(command.getLastName());
        
        modification.updater(entity::setEmail)
            .accessCheck(canModify)
            .currentValue(entity.getEmail())
            .path("email")
            .exception((e, msg) -> {
                if (e instanceof DuplicateEmailException) {
                    msg.accept(new Message("Email address is already in use"));
                }
            })
            .updateByCmd(command.getEmail(), email -> {
                validateEmailUnique(email, entity.getId());
                return email;
            });
        
        modification.updater(entity::setRoles)
            .accessCheck(canModify && hasRoleManagementPermission(context))
            .currentValue(entity.getRoles())
            .path("roleIds")
            .updateByCmd(command.getRoleIds(), roleIds -> {
                if (roleIds == null) return null;
                return roleRepository.findAllById(roleIds);
            });
    }
    
    private void validateEmailUnique(String email, Long excludeId) throws ProcessorException {
        if (StringSupport.empty(email)) return;
        
        UserEntity existing = userRepository.findByEmail(email);
        if (existing != null && !Objects.equals(existing.getId(), excludeId)) {
            throw new ProcessorException().add(ErrorType.VALIDATION, "email", "Email address is already in use");
        }
    }
}
```

## Reference Implementation

### Service Handler Usage

Client-side service handler for executing remoting calls:

```java
public class UserServiceHandler<T> extends AppServiceHandler<T> {
    
    // Lookup operations
    public static void lookupUser(UserRef ref, Consumer<UserLookupResult> onSuccess) {
        UserLookup.byRef(ref)
            .includeProfile(true)
            .includeRoles(true)
            .executeWith(new UserServiceHandler<UserLookupResult>()
                .onSuccessful(onSuccess)
                .onFailure((errors, status) -> {
                    showError("Failed to load user: " + errors.get(0).getMessage());
                }));
    }
    
    // Search operations
    public static void searchUsers(String keywords, int page, Consumer<UserQueryResultSet> onSuccess) {
        new UserQuery(page, 20)
            .keywords(keywords)
            .active(true)
            .sort(UserQuery.Sort.LAST_NAME_ASC)
            .executeWith(new UserServiceHandler<UserQueryResultSet>()
                .onSuccessful(onSuccess));
    }
    
    // Command operations
    public static void createUser(String firstName, String lastName, String email, 
                                Consumer<UserLookupResult> onSuccess) {
        UserConstruct construct = new UserConstruct(firstName, lastName, email);
        UserCommand command = new UserCommand(construct);
        
        executeCommand(command, onSuccess);
    }
    
    public static void updateUser(UserRef ref, Consumer<UserCommand> commandBuilder,
                                Consumer<UserLookupResult> onSuccess) {
        UserCommand command = new UserCommand(ref);
        commandBuilder.accept(command);
        
        executeCommand(command, onSuccess);
    }
    
    private static void executeCommand(UserCommand command, Consumer<UserLookupResult> onSuccess) {
        // Execute command and return updated user
        new UserServiceHandler<UserLookupResult>()
            .onSuccessful(result -> {
                showMessage("User saved successfully");
                onSuccess.accept(result);
            })
            .onFailure((errors, status) -> {
                showValidationErrors(errors);
            })
            .remoteExecute(UserLookup.byRef(command.lookup()), command);
    }
}
```

### Store Implementation

For use with tables and galleries:

```java
public class UserStore extends PaginatedStore<UserQueryResult> {
    
    private UserQuery query = new UserQuery(0, 20);
    
    @Override
    protected void requestLoad(int page, int pageSize, ILoadRequestCallback<UserQueryResult> cb) {
        query.setPage(page);
        query.setPageSize(pageSize);
        
        new AppServiceHandler<UserQueryResultSet>()
            .onSuccessful(v -> {
                cb.onSuccess(v.getResults(), v.getTotalResults(), query.filtering());
            })
            .onFailure((v, s) -> {
                String message = v.isEmpty() ? "Failed to load users" : v.get(0).getMessage();
                cb.onFailure(message);
            })
            .remoteExecute(query);
    }
    
    @Override
    protected void onClear() {
        query = new UserQuery(0, 20);
    }
    
    // Search operations
    public void searchByKeywords(String keywords) {
        query.keywords(keywords);
        reload(20);
    }
    
    public void filterByDepartment(String department) {
        query.department(department);
        reload(20);
    }
    
    public void filterByActive(Boolean active) {
        query.active(active);
        reload(20);
    }
    
    public void sortBy(UserQuery.Sort sort) {
        query.sort(sort);
        reload(20);
    }
    
    // Bulk operations
    public void query(Consumer<UserQuery> updater) {
        if (updater != null) {
            updater.accept(query);
        }
        reload(20);
    }
}
```

This comprehensive guide provides everything needed to implement consistent, maintainable remoting patterns in JUI applications. The patterns support type safety, clear separation of concerns, and robust error handling while enabling both simple and complex operations.