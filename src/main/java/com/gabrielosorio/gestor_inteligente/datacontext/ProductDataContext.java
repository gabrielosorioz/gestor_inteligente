package com.gabrielosorio.gestor_inteligente.datacontext;

import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.datacontext.base.DataContext;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class ProductDataContext implements DataContext<Product> {

    private static volatile ProductDataContext instance;
    private final Repository<Product> productRepository;

    // Estruturas de armazenamento
    private final List<Product> products;
    private final Map<Long, Product> idIndex = new ConcurrentHashMap<>();
    private final Map<String, Product> codeIndex = new ConcurrentHashMap<>();

    // Controle de concorrência
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private static final Logger log = Logger.getLogger(ProductDataContext.class.getName());

    private ProductDataContext(Repository<Product> productRepository) {
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
                return null;
            }

            // Valida unicidade de códigos
            validateUniqueCodes(product);

            productRepository.add(product);
            products.add(product);
            indexProduct(product);

            return product;
        } finally {
            writeLock.unlock();
        }
    }

    private void validateUniqueCodes(Product product) {
        if (codeIndex.containsKey(String.valueOf(product.getProductCode()))) {
            throw new IllegalStateException("Código do produto já existe");
        }
        product.getBarCode().ifPresent(bc -> {
            if (codeIndex.containsKey(bc)) {
                throw new IllegalStateException("Código de barras já existe");
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
            if (existing == null) return null;

            // Verifica mudanças nos códigos
            boolean productCodeChanged = existing.getProductCode() != newProduct.getProductCode();
            boolean barCodeChanged = !Objects.equals(existing.getBarCode(), newProduct.getBarCode());

            if (productCodeChanged || barCodeChanged) {
                validateUniqueCodes(newProduct);
                unindexProduct(existing);
            }

            // Atualiza na lista
            int index = products.indexOf(existing);
            products.set(index, newProduct);

            // Atualiza repositório e índices
            productRepository.update(newProduct);
            indexProduct(newProduct);

            return newProduct;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(long id) {
        writeLock.lock();
        try {
            Product product = idIndex.get(id);
            if (product == null) return false;

            productRepository.remove(id);
            products.remove(product);
            unindexProduct(product);

            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public static ProductDataContext getInstance(Repository<Product> productRepository) {
        if (instance == null) {
            synchronized (ConnectionFactory.class) {
                if (instance == null) {
                    instance = new ProductDataContext(productRepository);
                }
            }
        }
        return instance;
    }
}