package ae.adres.localize.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PluralTest {
    @Test
    fun defaultPlural() {
        assertEquals("one", selectPluralForm("en", 1))
        assertEquals("other", selectPluralForm("en", 5))
    }

    @Test
    fun arabicPlural() {
        assertEquals("zero", selectPluralForm("ar", 0))
        assertEquals("one", selectPluralForm("ar", 1))
        assertEquals("two", selectPluralForm("ar", 2))
        assertEquals("few", selectPluralForm("ar", 5))
        assertEquals("many", selectPluralForm("ar", 15))
        assertEquals("other", selectPluralForm("ar", 100))
    }

    @Test
    fun slavicPlural() {
        assertEquals("one", selectPluralForm("ru", 1))
        assertEquals("one", selectPluralForm("ru", 21))
        assertEquals("few", selectPluralForm("ru", 2))
        assertEquals("many", selectPluralForm("ru", 5))
    }

    @Test
    fun frenchPlural() {
        assertEquals("one", selectPluralForm("fr", 0))
        assertEquals("one", selectPluralForm("fr", 1))
        assertEquals("other", selectPluralForm("fr", 2))
    }
}
