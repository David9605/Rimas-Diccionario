package com.davidgonzalez.rimasdiccionario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RhymeEngineTest {
    private fun k(w: String) = RhymeEngine.analyze(w)!!

    @Test fun silabasYTerminacion() {
        assertEquals(2, k("casa").syllables)
        assertEquals("asa", k("casa").consonante)
        assertEquals("aa", k("casa").asonante)
    }

    @Test fun agudasConTilde() {
        assertEquals("on", k("canción").consonante)
        assertEquals(k("canción").consonante, k("acción").consonante)
    }

    @Test fun agudasSinTilde() {
        assertEquals(k("amor").consonante, k("dolor").consonante)
        assertEquals("or", k("amor").consonante)
    }

    @Test fun diptongosYHiatos() {
        assertEquals("oria", k("gloria").consonante)
        assertEquals(k("gloria").consonante, k("historia").consonante)
        assertEquals(k("día").consonante, k("María").consonante)
        assertEquals(k("ruido").consonante, k("ido").consonante)
    }

    @Test fun yFinal() {
        assertEquals(k("rey").consonante, k("ley").consonante)
    }

    @Test fun uMuda() {
        assertEquals(k("queso").consonante, k("peso").consonante)
    }

    @Test fun equivalenciaFonetica() {
        assertEquals(k("uva").consonante, k("tuba").consonante)
    }

    @Test fun asonanteSinConsonante() {
        assertEquals(k("claro").asonante, k("pato").asonante)
        assertNotEquals(k("claro").consonante, k("pato").consonante)
    }

    @Test fun palabraInvalida() {
        assertNull(RhymeEngine.analyze("dos palabras"))
        assertNull(RhymeEngine.analyze("123"))
    }

    @Test fun quitarTildes() {
        assertEquals("cancion", RhymeEngine.strip("canción"))
        assertEquals("año", RhymeEngine.strip("año"))
    }
}
