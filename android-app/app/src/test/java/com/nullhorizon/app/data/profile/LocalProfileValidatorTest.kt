package com.nullhorizon.app.data.profile

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LocalProfileValidatorTest {
    @Test
    fun normalize_trimsAndCollapsesWhitespace() {
        assertThat(LocalProfileValidator.normalize("  Ada   Venn ")).isEqualTo("Ada Venn")
    }

    @Test
    fun isValid_acceptsCallsign() {
        assertThat(LocalProfileValidator.isValid("Operator_7")).isTrue()
        assertThat(LocalProfileValidator.isValid("Ada Venn")).isTrue()
    }

    @Test
    fun isValid_rejectsTooShortOrSymbols() {
        assertThat(LocalProfileValidator.isValid("A")).isFalse()
        assertThat(LocalProfileValidator.isValid("bad@name")).isFalse()
        assertThat(LocalProfileValidator.isValid("")).isFalse()
    }
}
