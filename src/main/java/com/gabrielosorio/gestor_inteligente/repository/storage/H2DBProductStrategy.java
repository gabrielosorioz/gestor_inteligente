package com.gabrielosorio.gestor_inteligente.repository.storage;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.strategy.RepositoryStrategy;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class H2DBProductStrategy implements RepositoryStrategy<Product> {

    private static final Logger log = Logger.getLogger(H2DBProductStrategy.class.getName());
    private final ConnectionFactory connectionFactory = ConnectionFactory.getInstance();
    private static H2DBProductStrategy instance;
    final private List<Product> products;

    private H2DBProductStrategy(){
        products = getProducts();
    }


    public static H2DBProductStrategy getInstance(){
        synchronized (H2DBProductStrategy.class){
            if(Objects.isNull(instance)){
                instance = new H2DBProductStrategy();
            }
            return instance;
        }
    }


    @Override
    public Product add(Product product) {

        var query = "INSERT INTO `produtos` (idproduto, codbarras, custo, valor, quantidade, descricao) " +
                "VALUES (?,?,?,?,?,?)";

        try(PreparedStatement pstmt = connectionFactory.getConnection().prepareStatement(query)){

            pstmt.setLong(1,product.getProductCode());
            pstmt.setString(2,product.getBarCode().orElse(null));
            pstmt.setBigDecimal(3,product.getCostPrice());
            pstmt.setBigDecimal(4,product.getSellingPrice());
            pstmt.setLong(5,product.getQuantity());
            pstmt.setString(6,product.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows > 0){
                try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                    if(generatedKeys.next()){

                    }
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Optional<Product> find(long id) {
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        return products;
    }

    @Override
    public List<Product> findBySpecification(Specification<Product> specification) {

        return List.of();
    }

    @Override
    public Product update(Product newT) {
        return null;
    }

    @Override
    public boolean remove(long id) {

        return false;
    }

    private ArrayList<Product> getProducts(){
        final String query = "SELECT * FROM produtos";
        try(PreparedStatement pstmt = connectionFactory.getConnection().prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {

            if(products != null){
                if(products.isEmpty()){
                    products.clear();
                }
            }

            final var prodList = new ArrayList<Product>();

            while(rs.next()){
                var product = mapRowToProduct(rs);
                prodList.add(product);
            }
            return prodList;

        } catch (SQLException e) {
            log.info("Não foi possível carregar os produtos: " + e.getMessage() + e.getCause());
            throw new RuntimeException(e);
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        var product = Product.builder()
                .productCode(rs.getLong("idproduto"))
                .description(rs.getString("descricao"))
                .barCode(Optional.ofNullable(rs.getString("codbarras")))
                .costPrice(rs.getBigDecimal("custo"))
                .sellingPrice(rs.getBigDecimal("valor"))
                .quantity(rs.getLong("quantidade"))
                .dateCreate(Timestamp.from(Instant.now()))
                .build();
       return product;
    }

}
