package com.devsuperior.dsmovie.services;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;
	
	@Mock
	private MovieRepository movieRepository;
	
	private Long existingId, nonExistingId, dependentId;
	private MovieEntity movieEntity;
	private String movieTitle;
	private PageImpl<MovieEntity> page;
	private MovieDTO movieDTO;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		
		movieTitle = "The Witcher";
		movieEntity = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movieEntity);
		page = new PageImpl<>(List.of(movieEntity));
		
		Mockito.when(movieRepository.findById(existingId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingId)).thenReturn(Optional.empty());
		
		Mockito.when(movieRepository.searchByTitle(any(), (Pageable) any())).thenReturn(page);
		
		Mockito.when(movieRepository.save(any())).thenReturn(movieEntity);
		
		Mockito.when(movieRepository.getReferenceById(existingId)).thenReturn(movieEntity);
		Mockito.when(movieRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		Mockito.when(movieRepository.existsById(existingId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(dependentId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistingId)).thenReturn(false);
		
		Mockito.doNothing().when(movieRepository).deleteById(existingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentId);
	}
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		
		Pageable pageable = PageRequest.of(0, 12);
		Page<MovieDTO> response = service.findAll(movieTitle, pageable);
		
		Assertions.assertNotNull(response);
		Assertions.assertEquals(response.getSize(), 1);
		Assertions.assertEquals(response.iterator().next().getTitle(), movieEntity.getTitle());
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		
		MovieDTO movieDTO = service.findById(existingId);
		
		Assertions.assertNotNull(movieDTO);
		Assertions.assertEquals(movieDTO.getId(), movieEntity.getId());
		Assertions.assertEquals(movieDTO.getTitle(), movieEntity.getTitle());
		Assertions.assertEquals(movieDTO.getScore(), movieEntity.getScore());
		
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		
		MovieDTO obj = service.insert(movieDTO);
		
		Assertions.assertNotNull(obj);
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		
		MovieDTO obj = service.update(existingId, movieDTO);
		
		Assertions.assertNotNull(obj);
		Assertions.assertEquals(obj.getId(), movieDTO.getId());
		Assertions.assertEquals(obj.getCount(), movieDTO.getCount());
		Assertions.assertEquals(obj.getScore(), movieDTO.getScore());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
	}
}
