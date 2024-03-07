package com.devsuperior.dsmovie.services;

import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

	@InjectMocks
	private ScoreService service;

	@Mock
	private UserService userService;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private ScoreRepository scoreRepository;

	private MovieEntity movieEntity;
	private ScoreEntity scoreEntity;
	private UserEntity userEntity;

	private Long existingMovieId, nonExistingMovieId;

	@BeforeEach
	void setUp() throws Exception {
		existingMovieId = 1L;
		nonExistingMovieId = 2L;

		scoreEntity = ScoreFactory.createScoreEntity();
		userEntity = UserFactory.createUserEntity();
		movieEntity = MovieFactory.createMovieEntity();

		Mockito.when(userService.authenticated()).thenReturn(userEntity);
		
		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());
		
		Mockito.when(scoreRepository.saveAndFlush(any())).thenReturn(scoreEntity);
		Mockito.when(movieRepository.save(any())).thenReturn(movieEntity);
	}

	@Test
	public void saveScoreShouldReturnMovieDTO() {
		
		ScoreDTO obj = new ScoreDTO(scoreEntity);
		
		MovieDTO result = service.saveScore(obj);
		
		Assertions.assertNotNull(result);
		
	}

	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

		Mockito.doThrow(ResourceNotFoundException.class).when(movieRepository).findById(existingMovieId);
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.saveScore(new ScoreDTO(nonExistingMovieId, scoreEntity.getValue()));
		});
	}
}
