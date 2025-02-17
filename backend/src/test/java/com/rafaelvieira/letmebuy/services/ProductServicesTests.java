package com.rafaelvieira.letmebuy.services;

import com.rafaelvieira.letmebuy.dto.ProductDTO;
import com.rafaelvieira.letmebuy.entities.Product;
import com.rafaelvieira.letmebuy.repository.ProductRepository;
import com.rafaelvieira.letmebuy.services.handlers.DataBaseException;
import com.rafaelvieira.letmebuy.services.handlers.ResourceNotFoundException;
import com.rafaelvieira.letmebuy.tests.Factory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ProductServicesTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private Product product;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));

        Mockito.when(repository.findAll((Pageable)any())).thenReturn(page);

        Mockito.when(repository.save(any())).thenReturn(product);

        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(repository.find(any(), any(), any())).thenReturn(page);

        //Teste com ID existente na base (Remover o mockito o sistema importa staticamente os metodos)
        Mockito.doNothing().when(repository).deleteById(existingId);
        //Teste quando não existe o ID na base lança uma exception
        Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
        //Verifica a integridade se tem uma categoria associado ao produto que esta tentando excluir(porem a categoria
        //Esta idependente uma classe distinta)
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingId);
        });
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists(){
        ProductDTO result = service.findById(existingId);
        Assertions.assertNotNull(result);
    }

    @Test
    public void findAllPagedShouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 12);

        Page<ProductDTO> result = service.findAllPagedWithFeedbacks(0L, "", pageable);

        Assertions.assertNotNull(result);

        //Mockito.verify(repositories, times(1)).find(pageable);
    }

    @Test
    public void deleteShouldThrowDataBaseExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(DataBaseException.class, () -> {
            service.delete(dependentId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
    }

}
