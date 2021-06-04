package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServicesTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository repository;

	@Mock
	private CategoryRepository categoryRepository;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private Category category;
	private ProductDTO productDTO;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		productDTO = Factory.createProductDTO();

		page = new PageImpl<>(List.of(product));

		// Behavior of mock repository: ProductRepository

		Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

		Mockito.when(repository.getOne(existingId)).thenReturn(product);
		Mockito.when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

		// Behavior of mock repository: CategoryRepository
		Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

	}

	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			ProductDTO result = service.update(nonExistingId, productDTO);
		});

		Mockito.verify(repository, Mockito.times(1)).getOne(nonExistingId);

	}

	@Test
	public void updateShouldReturnProductDTOWhenIdExists() {

		ProductDTO result = service.update(existingId, productDTO);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(productDTO.getId(), result.getId());

		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).getOne(existingId);
		Mockito.verify(categoryRepository, Mockito.times(1)).getOne(category.getId());
	}

	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {

		ProductDTO result = service.findById(existingId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(product.getId(), result.getId());

		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).findById(existingId);
	}

	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			ProductDTO result = service.findById(nonExistingId);
		});

		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).findById(nonExistingId);
	}

	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> result = service.findAllPaged(pageable);
		Assertions.assertNotNull(result);
		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).findAll(pageable);

	}

	@Test
	public void deleteShouldThrowDatabaseExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);

	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);

	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		// Check (how many) if the method of mock repository was invoked .
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);

	}

}
