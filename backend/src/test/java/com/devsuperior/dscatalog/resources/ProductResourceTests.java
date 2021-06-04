package com.devsuperior.dscatalog.resources;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;

	private PageImpl<ProductDTO> page;
	private ProductDTO productDTO;

	@BeforeEach
	void setup() throws Exception {

		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;

		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));

		Mockito.when(service.findAllPaged((Pageable) ArgumentMatchers.any())).thenReturn(page);

		Mockito.when(service.findById(existingId)).thenReturn(productDTO);
		Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		Mockito.when(service.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any())).thenReturn(productDTO);
		Mockito.when(service.update(ArgumentMatchers.eq(nonExistingId),ArgumentMatchers.any())).thenThrow(ResourceNotFoundException.class);
		
		//when a method is void the order is different.
		Mockito.doNothing().when(service).delete(existingId);
		Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);  
		
		Mockito.when(service.insert(ArgumentMatchers.any())).thenReturn(productDTO);
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenIdExits() throws Exception {
		ResultActions result = mockMvc.
				perform(MockMvcRequestBuilders.delete("/products/{id}", existingId));
		result.andExpect(status().isNoContent());		
	}
	
	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		ResultActions result = mockMvc.
				perform(MockMvcRequestBuilders.delete("/products/{id}", nonExistingId));
		
		result.andExpect(status().isNotFound());	
	}

	@Test
	public void deleteShouldReturnBadRequestWhenIdIsDependent() throws Exception {
		ResultActions result = mockMvc.
				perform(MockMvcRequestBuilders.delete("/products/{id}", dependentId)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isBadRequest());	
	}
	
	
	@Test
	public void insertShouldReturnProductDTOCreated() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.post("/products")
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());	
	}
	
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.put("/products/{id}", existingId)
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());	
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.put("/products/{id}",nonExistingId )
				.content(jsonBody).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
		
	}
	

	@Test
	public void findAllShouldReturnPage() throws Exception {

		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.get("/products").accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());

		// or it can be this direct way too:
		// mockMvc.perform(MockMvcRequestBuilders.get("/products").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		ResultActions result = mockMvc.perform(
				MockMvcRequestBuilders.get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}

}
