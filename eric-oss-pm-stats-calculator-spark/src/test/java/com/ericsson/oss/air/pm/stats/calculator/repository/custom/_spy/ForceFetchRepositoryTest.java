/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.custom._spy;

import static org.mockito.Mockito.doReturn;

import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.repository.custom.ForceFetchRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForceFetchRepositoryTest {
    @Spy ForceFetchRepository<String, Integer> objectUnderTest;

    @Test
    void shouldForceFetch() {
        doReturn(Optional.of("hello")).when(objectUnderTest).findById(1);

        final String actual = objectUnderTest.forceFetchById(1);

        Assertions.assertThat(actual).isEqualTo("hello");
    }

    @Test
    void shouldThrowException_whenEntityIsNotFound() {
        doReturn(Optional.empty()).when(objectUnderTest).findById(1);

        Assertions.assertThatThrownBy(() -> objectUnderTest.forceFetchById(1))
                  .isExactlyInstanceOf(EntityNotFoundException.class)
                  .hasMessage("Entity with id '1' is not found.");
    }
}