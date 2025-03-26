package com.gabrielosorio.gestor_inteligente.datacontext;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.datacontext.base.DataContext;
import com.gabrielosorio.gestor_inteligente.exception.DuplicateProductException;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.base.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.specification.base.Specification;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ProductDataContext implements DataContext<Product> {

    private static volatile ProductDataContext instance;
    private final ProductRepository productRepository;

    private final List<Product> products;
    private final Map<Long, Product> idIndex = new ConcurrentHashMap<>();
    private final Map<String, Product> codeIndex = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private static final Logger log = Logger.getLogger(ProductDataContext.class.getName());

    private ProductDataContext(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.products = Collections.synchronizedList(new ArrayList<>());
        initializeData();
    }


    private void initializeData() {
        writeLock.lock();
        try {
            List<Product> dbProducts = productRepository.findAll();
            products.addAll(dbProducts);
            rebuildIndexes();
            log.info("Data context initialized with " + dbProducts.size() + " products");
        } finally {
            writeLock.unlock();
        }
    }

    private void rebuildIndexes() {
        codeIndex.clear();
        idIndex.clear();
        products.forEach(this::indexProduct);
    }

    // Indexação completa do produto
    private void indexProduct(Product product) {
        // ID único e imutável
        idIndex.put(product.getId(), product);

        // Códigos mutáveis
        indexProductCode(product.getProductCode(), product);
        product.getBarCode().ifPresent(bc -> indexProductCode(bc, product));
    }

    private void indexProductCode(Object code, Product product) {
        codeIndex.put(String.valueOf(code), product);
    }

    // Remoção segura de índices
    private void unindexProduct(Product product) {
        // Remove código do produto
        codeIndex.remove(String.valueOf(product.getProductCode()));

        // Remove código de barras se existir
        product.getBarCode().ifPresent(codeIndex::remove);
    }

    @Override
    public Product add(Product product) {
        writeLock.lock();
        try {
            if (product == null || idIndex.containsKey(product.getId())) {
                log.warning("Attempt to add a null or duplicate product.");
                return null;
            }

            // Valida unicidade de códigos
            validateUniqueCodes(product);

            productRepository.add(product);
            products.add(product);
            indexProduct(product);

            log.info("Product added: " + product);
            return product;
        } catch (DuplicateProductException e) {
            log.severe("Duplicate product error: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    private void validateUniqueCodes(Product product) {
        String pCode = String.valueOf(product.getProductCode());

        if (codeIndex.containsKey(pCode)) {
            log.severe("Product code:[%s] already exists.".formatted(pCode));
            throw new DuplicateProductException("Product code:[%s] already exists.".formatted(pCode));
        }
        product.getBarCode().ifPresent(bc -> {
            if (codeIndex.containsKey(bc)) {
                log.severe("Barcode:[%d] already exists.".formatted(bc));
                throw new DuplicateProductException("Barcode:[%d] already exists.".formatted(bc));
            }
        });
    }

    private void validateUniqueCodes(Product newProduct, Product existingProduct) {
        String productCode = String.valueOf(newProduct.getProductCode());
        Product existingByCode = codeIndex.get(productCode);

        // Se o código existe e não pertence ao produto atual, lança exceção
        if (existingByCode != null &&
                (existingProduct == null || existingByCode.getId() != existingProduct.getId())) {
            throw new DuplicateProductException("Product code:[%s] already exists.".formatted(productCode));
        }

        // Valida código de barras
        newProduct.getBarCode().ifPresent(barcode -> {
            Product existingByBarcode = codeIndex.get(barcode);
            if (existingByBarcode != null &&
                    (existingProduct == null || existingByBarcode.getId() != existingProduct.getId())) {
                throw new DuplicateProductException("Barcode:[%s] already exists.".formatted(barcode));
            }
        });
    }

    @Override
    public Optional<Product> find(long id) {
        readLock.lock();
        try {
            return Optional.ofNullable(idIndex.get(id));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<Product> findByCode(String code) {
        readLock.lock();
        try {
            return Optional.ofNullable(codeIndex.get(code));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Product> findAll() {
        readLock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(products));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Product> findBySpecification(Specification<Product> specification) {
        readLock.lock();
        try {
            return productRepository.findBySpecification(specification);
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public Product update(Product newProduct) {
        writeLock.lock();
        try {
            Product existing = idIndex.get(newProduct.getId());
            if (existing == null) {
                log.warning("Attempting to update a non-existent product.");
                return null;
            }

            updateProductCodes(existing, newProduct);
            existing.setDescription(newProduct.getDescription());
            existing.updatePrices(newProduct.getCostPrice(), newProduct.getSellingPrice());
            existing.setSupplier(newProduct.getSupplier());
            existing.setCategory(newProduct.getCategory());
            existing.setQuantity(newProduct.getQuantity());
            existing.setStatus(newProduct.getStatus());
            existing.setDateCreate(newProduct.getDateCreate());
            existing.setDateUpdate(newProduct.getDateUpdate());
            existing.setDateDelete(newProduct.getDateDelete());
            productRepository.update(existing);
            log.info("Produto atualizado: " + existing);
            return existing;
        } finally {
            writeLock.unlock();
        }
    }

    private void updateProductCodes(Product existing, Product newProduct) {
        boolean productCodeChanged = existing.getProductCode() != newProduct.getProductCode();
        boolean barCodeChanged = !Objects.equals(existing.getBarCode(), newProduct.getBarCode());

        // Se qualquer código foi alterado, realiza a validação e a reindexação
        if (productCodeChanged || barCodeChanged) {
            validateUniqueCodes(newProduct, existing);
            if (productCodeChanged) existing.setProductCode(newProduct.getProductCode());
            if (barCodeChanged) existing.setBarCode(newProduct.getBarCode());
            unindexProduct(existing);
            indexProduct(existing);
        }
    }



    @Override
    public boolean remove(long id) {
        writeLock.lock();
        try {
            Product product = idIndex.get(id);
            if (product == null) {
                log.warning("Attempt to remove a non-existent product.");
                return false;
            }

            productRepository.remove(id);
            products.remove(product);
            unindexProduct(product);

            log.info("Product removed: " + product);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean existsByProductCode(long productCode) {
        readLock.lock();
        try {
            return codeIndex.containsKey(String.valueOf(productCode));
        } finally {
            readLock.unlock();
        }
    }


    public static ProductDataContext getInstance(ProductRepository productRepository) {
        if (instance == null) {
            synchronized (ConnectionFactory.class) {
                if (instance == null) {
                    instance = new ProductDataContext(productRepository);
                }
            }
        }
        return instance;
    }

    // Formatter personalizado
    static class CustomLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            // Formato personalizado: [Nível] Mensagem (Timestamp)
            return String.format("[%s] [%s] %s (%s)%n",
                    record.getLevel(),
                    Thread.currentThread().getName(),
                    record.getMessage(),
                    new Date(record.getMillis()));
        }
    }
}