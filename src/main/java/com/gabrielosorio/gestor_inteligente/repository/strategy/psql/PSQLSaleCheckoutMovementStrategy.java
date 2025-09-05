package com.gabrielosorio.gestor_inteligente.repository.strategy.psql;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.config.DBScheme;
import com.gabrielosorio.gestor_inteligente.config.QueryLoader;
import com.gabrielosorio.gestor_inteligente.model.*;
import com.gabrielosorio.gestor_inteligente.model.enums.CheckoutStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.PaymentMethod;
import com.gabrielosorio.gestor_inteligente.model.enums.SaleStatus;
import com.gabrielosorio.gestor_inteligente.model.enums.Status;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.BatchInsertable;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.strategy.base.TransactionalRepositoryStrategyV2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQLSaleCheckoutMovementStrategy extends TransactionalRepositoryStrategyV2<SaleCheckoutMovement,Long> implements RepositoryStrategy<SaleCheckoutMovement,Long>, BatchInsertable<SaleCheckoutMovement> {

    private final QueryLoader qLoader;
    private final Logger log = Logger.getLogger(getClass().getName());

    public PSQLSaleCheckoutMovementStrategy(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        this.qLoader = new QueryLoader(DBScheme.POSTGRESQL);
    }

    /**
     * Maps a ResultSet from a complex join query to a list of SaleCheckoutMovement objects with proper object relationships
     * avoiding duplication of objects when they're referenced multiple times.
     */
    public List<SaleCheckoutMovement> mapResultSetEager(ResultSet rs) throws SQLException {
        Map<Long, Sale> salesMap = new HashMap<>();
        Map<Long, CheckoutMovement> checkoutMovementsMap = new HashMap<>();
        Map<Long, Payment> paymentsMap = new HashMap<>();
        Map<Long, Checkout> checkoutsMap = new HashMap<>();
        Map<Long, SaleCheckoutMovement> saleCheckoutMovementsMap = new HashMap<>();
        Map<Long, Map<Long, SaleProduct>> saleProductsMap = new HashMap<>();
        Map<Long, Product> productsMap = new HashMap<>();
        Map<Long, Category> categoriesMap = new HashMap<>();
        Map<Long, Supplier> suppliersMap = new HashMap<>();

        while (rs.next()) {
            // Extract SaleCheckoutMovement data
            long scmId = rs.getLong("id");
            long checkoutMovementId = rs.getLong("checkout_movement_id");
            long saleId = rs.getLong("sale_id");

            // Process Payment if not already in cache
            long paymentId = rs.getLong("payment_id");
            if (!paymentsMap.containsKey(paymentId)) {
                Payment payment = new Payment(PaymentMethod.getMethodById(paymentId));
                payment.setId(paymentId);
                payment.setDescription(rs.getString("payment_description"));
                paymentsMap.put(paymentId, payment);
            }

            // Process Checkout if not already in cache
            long checkoutId = rs.getLong("checkout_id");
            if (!checkoutsMap.containsKey(checkoutId)) {
                Checkout checkout = new Checkout();

                new Checkout(
                        rs.getLong("id"),
                        CheckoutStatus.fromDescription(rs.getString("checkout_status")),
                        rs.getTimestamp("checkout_opened_at").toLocalDateTime(),
                        rs.getTimestamp("checkout_closed_at") != null ? rs.getTimestamp("checkout_closed_at").toLocalDateTime() : null,
                        rs.getBigDecimal("checkout_initial_cash"),
                        rs.getBigDecimal("checkout_total_entry"),
                        rs.getBigDecimal("checkout_total_exit"),
                        rs.getBigDecimal("checkout_closing_balance"),
                        new User(),
                        new User(),
                        rs.getTimestamp("checkout_created_at") != null ? rs.getTimestamp("checkout_created_at").toLocalDateTime() : null,
                        rs.getTimestamp("checkout_updated_at") != null ? rs.getTimestamp("checkout_updated_at").toLocalDateTime() : null
                );

                checkoutsMap.put(checkoutId, checkout);
            }

            // Process CheckoutMovement if not already in cache
            if (!checkoutMovementsMap.containsKey(checkoutMovementId)) {
                CheckoutMovement checkoutMovement = new CheckoutMovement();
                checkoutMovement.setId(checkoutMovementId);
                checkoutMovement.setCheckout(checkoutsMap.get(checkoutId));
                checkoutMovement.setDateTime(rs.getTimestamp("checkoutmovement_date_time").toLocalDateTime());
                checkoutMovement.setValue(rs.getBigDecimal("checkoutmovement_value"));
                checkoutMovement.setObs(rs.getString("checkoutmovement_obs"));
                checkoutMovement.setPayment(paymentsMap.get(paymentId));
//                checkoutMovement.setMovementTypeId(rs.getLong("checkoutmovement_checkoutmovement_type_id"));
                checkoutMovementsMap.put(checkoutMovementId, checkoutMovement);
            }

            // Process Category if not already in cache
            Long categoryId = rs.getLong("category_id");
            if (rs.wasNull()) {
                categoryId = null;
            }

            if (categoryId != null && !categoriesMap.containsKey(categoryId)) {
                Category category = new Category(categoryId,rs.getString("category_description"));
                categoriesMap.put(categoryId, category);
            }

            // Process Supplier if not already in cache
            Long supplierId = rs.getLong("supplier_id");
            if (rs.wasNull()) {
                supplierId = null;
            }

            if (supplierId != null && !suppliersMap.containsKey(supplierId)) {
                Supplier supplier = new Supplier();
                supplier.setId(supplierId);
                supplier.setCellPhone(Optional.ofNullable(rs.getString("supplier_cell_phone")));
                supplier.setName(rs.getString("supplier_name"));
                supplier.setAddress(Optional.ofNullable(rs.getString("supplier_address")));
                supplier.setCnpj(Optional.ofNullable(rs.getString("supplier_cnpj")));
                supplier.setEmail(Optional.ofNullable(rs.getString("supplier_email")));
                suppliersMap.put(supplierId, supplier);
            }

            // Process Product if not already in cache
            long productId = rs.getLong("product_product_id");
            if (!productsMap.containsKey(productId)) {
                Category category = null;
                Supplier supplier = null;

                if (categoryId != null) {
                    category = categoriesMap.get(categoryId);
                }

                if (supplierId != null) {
                    supplier = suppliersMap.get(supplierId);
                }


                Product product = Product.builder()
                        .id(rs.getLong("product_product_id"))
                        .description(rs.getString("product_description"))
                        .quantity(rs.getLong("product_quantity"))
                        .barCode(Optional.ofNullable(rs.getString("product_bar_code")))
                        .supplier(Optional.ofNullable(supplier))
                        .sellingPrice(rs.getBigDecimal("product_selling_price"))
                        .dateDelete(rs.getTimestamp("product_date_delete"))
                        .costPrice(rs.getBigDecimal("product_cost_price"))
                        .category(Optional.ofNullable(category))
                        .dateUpdate(rs.getTimestamp("product_date_update"))
                        .status(Status.valueOf(rs.getString("product_status")))
                        .productCode(rs.getLong("product_product_code"))
                        .dateCreate(rs.getTimestamp("product_date_create"))
                        .build();


                productsMap.put(productId, product);
            }

            // Process Sale if not already in cache
            if (!salesMap.containsKey(saleId)) {
                Sale sale = new Sale();
                sale.setId(saleId);
                sale.setOriginalTotalPrice(rs.getBigDecimal("sale_originaltotalprice"));
                sale.setTotalDiscount(rs.getBigDecimal("sale_totaldiscount"));
                sale.setTotalPrice(rs.getBigDecimal("sale_totalprice"));
                sale.setStatus(SaleStatus.valueOf(rs.getString("sale_status")));
                sale.setDataCancel(rs.getTimestamp("sale_datacancel"));
                sale.setDateSale(rs.getTimestamp("sale_datesale"));
                sale.setTotalAmountPaid(rs.getBigDecimal("sale_totalamountpaid"));

                // Initialize products collection for this sale
                saleProductsMap.put(saleId, new HashMap<>());
                salesMap.put(saleId, sale);
            }

            // Process SaleProduct if not already in cache for this sale
            long saleProductId = rs.getLong("saleproduct_id");
            Map<Long, SaleProduct> productsBySale = saleProductsMap.get(saleId);

            if (!productsBySale.containsKey(saleProductId)) {
                SaleProduct saleProduct = new SaleProduct();
                saleProduct.setId(saleProductId);
                saleProduct.setUnitPrice(rs.getBigDecimal("saleproduct_unitprice"));
                saleProduct.setSaleId(saleId);
                saleProduct.setSubTotal(rs.getBigDecimal("saleproduct_subtotal"));
                saleProduct.setProduct(productsMap.get(productId));
                saleProduct.setQuantity(rs.getInt("saleproduct_quantity"));
                saleProduct.setOriginalSubtotal(rs.getBigDecimal("saleproduct_originalsubtotal"));
                saleProduct.setDiscount(rs.getBigDecimal("saleproduct_discount"));

                productsBySale.put(saleProductId, saleProduct);
            }

            // Process SaleCheckoutMovement if not already in cache
            if (!saleCheckoutMovementsMap.containsKey(scmId)) {
                SaleCheckoutMovement scm = new SaleCheckoutMovement(
                        scmId,
                        checkoutMovementsMap.get(checkoutMovementId),
                        salesMap.get(saleId)
                );
                saleCheckoutMovementsMap.put(scmId, scm);
            }
        }

        // Assign all SaleProducts to their respective Sale objects
        for (Map.Entry<Long, Sale> entry : salesMap.entrySet()) {
            Long saleId = entry.getKey();
            Sale sale = entry.getValue();

            Map<Long, SaleProduct> productsBySale = saleProductsMap.get(saleId);
            if (productsBySale != null) {
                sale.setSaleProducts(new ArrayList<>(productsBySale.values()));
            }
        }

        // Return the result list of SaleCheckoutMovement objects
        return new ArrayList<>(saleCheckoutMovementsMap.values());
    }

    @Override
    public SaleCheckoutMovement add(SaleCheckoutMovement saleCheckoutMovement) {
        var query = qLoader.getQuery("insertSaleCheckoutMovement");
        Connection connection = null;
        try {
            connection = getConnection();
            try(var ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){

                ps.setLong(1,saleCheckoutMovement.getCheckoutMovement().getId());
                ps.setLong(2,saleCheckoutMovement.getSale().getId());
                ps.executeUpdate();

                try(var gKeys = ps.getGeneratedKeys()){
                    if(gKeys.next()){
                        saleCheckoutMovement.setId(gKeys.getLong("id"));
                        log.info("SaleCheckoutMovement successfully inserted.");
                    } else {
                        throw new SQLException("Failed to insert sale checkout movement, no key generated.");
                    }
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to insert sale checkout movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to insert sale checkout movement", e);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }

        return saleCheckoutMovement;
    }

    @Override
    public Optional<SaleCheckoutMovement> find(Long id) {
        var query = qLoader.getQuery("findSaleCheckoutMovementById");
        Connection connection = null;
        try {
            connection = getConnection();

            try (var ps = connection.prepareStatement(query)){
                ps.setLong(1, id);
                try(var rs = ps.executeQuery()){
                    List<SaleCheckoutMovement> results = mapResultSetEager(rs);
                    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find Sale Checkout. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Sale Checkout search error. ", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<SaleCheckoutMovement> findAll() {
        var query = qLoader.getQuery("findAllSaleCheckoutMovements");
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query);
                 var rs = ps.executeQuery()){

                return mapResultSetEager(rs);

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find all sale checkout movements. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Sale Checkout Movements find all error. ", e);
            }

        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<SaleCheckoutMovement> findBySpecification(Specification<SaleCheckoutMovement> specification) {
        var query = specification.toSql();
        var params = specification.getParameters();
        Connection connection = null;

        try {
            connection = getConnection();
            try (var ps = connection.prepareStatement(query)){

                if(params.size() != ps.getParameterMetaData().getParameterCount()){
                    throw new SQLException("Mismatch between provided parameters and expected query parameters. " + query + params);
                }

                for(int i = 0; i < params.size(); i++){
                    ps.setObject(i + 1, params.get(i));
                }

                try(var rs = ps.executeQuery()){
                    return mapResultSetEager(rs);
                }

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to find sale checkout movement by specification. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Sale Checkout Movement find by specification error. ", e);
            }

        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public SaleCheckoutMovement update(SaleCheckoutMovement saleCheckoutMovement) {
        var query = qLoader.getQuery("updateSaleCheckoutMovement");
        Connection connection = null;
        try {
            connection = getConnection();

            try(var ps = connection.prepareStatement(query)){

                ps.setLong(1, saleCheckoutMovement.getCheckoutMovement().getId());
                ps.setLong(2, saleCheckoutMovement.getSale().getId());
                ps.setLong(3, saleCheckoutMovement.getId());

                int affectedRows = ps.executeUpdate();

                if(affectedRows == 0){
                    throw new SQLException("Failed to update sale checkout movement, no rows affected.");
                }

                log.info("Sale Checkout movement successfully updated.");

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to update sale checkout movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException("Failed to update sale checkout movement", e);
            }

        } catch (SQLException e){
            log.log(Level.SEVERE, "Failed to update sale checkout movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
            throw new RuntimeException("Failed to update sale checkout movement", e);
        } finally {
            closeConnection(connection);
        }

        return saleCheckoutMovement;
    }

    @Override
    public boolean remove(Long id) {
        var query = qLoader.getQuery("deleteSaleCheckoutMovementById");
        Connection connection = null;

        try {
            connection = getConnection();
            try(var ps = connection.prepareStatement(query)){

                ps.setLong(1,id);

                int affectedRows = ps.executeUpdate();

                if(affectedRows == 0){
                    log.warning("No Sale Checkout Movement found with id: " + id);
                    return false;
                }

                log.info("Sale Checkout Movement with id " + id + " successfully deleted.");
                return true;

            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to delete Sale Checkout Movement. {0} {1} {2}", new Object[]{e.getMessage(), e.getCause(), e.getSQLState()});
                throw new RuntimeException(e);
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }

    }

    @Override
    public List<SaleCheckoutMovement> addAll(List<SaleCheckoutMovement> saleCheckoutMovements) {
        var query = qLoader.getQuery("insertSaleCheckoutMovement");
        Connection connection = null;

        try {
            connection = getConnection();

            try(var ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){

                for(SaleCheckoutMovement saleCheckoutMovement: saleCheckoutMovements){
                    ps.setLong(1,saleCheckoutMovement.getCheckoutMovement().getId());
                    ps.setLong(2,saleCheckoutMovement.getSale().getId());
                    ps.addBatch();
                }

                ps.executeBatch();

                try(var gKeys = ps.getGeneratedKeys()){
                    int index = 0;
                    while(gKeys.next()){
                        saleCheckoutMovements.get(index++).setId(gKeys.getLong("id"));
                    }
                }

                log.info("All SaleCheckoutMovements successfully inserted.");
            } catch (SQLException e){
                    try {
                        connection.rollback();
                        log.warning("Transaction rolled back due to an error: " + e.getMessage());
                    } catch (SQLException rollBackEx) {
                        log.severe("RollBack failed " + rollBackEx.getMessage());
                    }
                throw new RuntimeException("Failed to batch insert SaleCheckoutMovements. " + e.getMessage(),e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection " + e.getMessage(),e);
        } finally {
            assert connection != null;
            closeConnection(connection);
        }
        return saleCheckoutMovements;
    }
}
