package com.gabrielosorio.gestor_inteligente.datacontext;
import com.gabrielosorio.gestor_inteligente.config.ConnectionFactory;
import com.gabrielosorio.gestor_inteligente.model.Product;
import com.gabrielosorio.gestor_inteligente.repository.ProductRepository;
import com.gabrielosorio.gestor_inteligente.repository.Repository;
import com.gabrielosorio.gestor_inteligente.repository.specification.Specification;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductDataContext implements DataContext<Product> {

    private static ProductDataContext instance;
    private final Repository<Product> productRepository;
    private final List<Product> products;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private static final Logger log = Logger.getLogger(ProductDataContext.class.getName());

    private ProductDataContext(Repository<Product> productRepository) {
        this.productRepository = productRepository;
        this.products = Collections.synchronizedList(productRepository.findAll());
    }


    public Product add(Product product) {
        writeLock.lock();
        try {
            if (product != null && !products.contains(product)) {
                // Checks if the product with the same ID already exists
                Optional<Product> existingProduct = products.stream()
                        .filter(p -> p.getId() == product.getId())
                        .findFirst();

                if (existingProduct.isEmpty()) {
                    productRepository.add(product);
                    products.add(product);
                    return product;
                } else {
                    log.warning("Product with ID: " + product.getId() + " already exists.");
                    return null;
                }
            }
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<Product> find(long id) {
        readLock.lock();
        try {
            return products.stream().filter(product -> product.getId() == id).findFirst();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Product> findAll() {
        readLock.lock();
        try {
            return Collections.unmodifiableList(products);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Product> findBySpecification(Specification<Product> specification) {
        readLock.lock();
        try {
            return productRepository.findBySpecification(specification);

            /** The option below has been discontinued because if the list is too long,
             * the operation becomes too costly */

            // Filters the list of products using the specification provided
            // return products.stream()
            //       .filter(specification::isSatisfiedBy)
            //        .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public Product update(Product newP) {
        writeLock.lock();
        try {
            for(int i = 0; i < products.size(); i++){
                if(products.get(i).getId() == newP.getId()){
                    productRepository.update(newP);
                    products.set(i,newP);
                    return newP;
                }
            }
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(long id) {
        writeLock.lock();
        try {
            Optional<Product> productToRemove = find(id);
            if (productToRemove.isPresent()) {
                productRepository.remove(id);
                products.remove(productToRemove.get());
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    public static ProductDataContext getInstance(Repository<Product> productRepository){
        synchronized (ConnectionFactory.class){
            if(Objects.isNull(instance)){
                instance = new ProductDataContext(productRepository);
            } else {
                log.info("Instance already exists. ");
            }
            return instance;
        }
    }

}
