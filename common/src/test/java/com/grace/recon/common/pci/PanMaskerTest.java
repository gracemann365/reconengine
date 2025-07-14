package com.grace.recon.common.pci;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PanMaskerTest {

  @Test
  void testMaskPan_validPan() {
    assertEquals("************1234", PanMasker.maskPan("1234567890121234"));
  }

  @Test
  void testMaskPan_shortPan() {
    assertEquals("123", PanMasker.maskPan("123"));
  }

  @Test
  void testMaskPan_nullPan() {
    assertNull(PanMasker.maskPan(null));
  }

  @Test
  void testMaskPan_emptyPan() {
    assertEquals("", PanMasker.maskPan(""));
  }

  @Test
  void testMaskPanShowFirstSixLastFour_validPan() {
    assertEquals("123456******1234", PanMasker.maskPanShowFirstSixLastFour("1234567890121234"));
  }

  @Test
  void testMaskPanShowFirstSixLastFour_shortPan() {
    assertEquals("123456789", PanMasker.maskPanShowFirstSixLastFour("123456789"));
  }

  @Test
  void testMaskPanShowFirstSixLastFour_nullPan() {
    assertNull(PanMasker.maskPanShowFirstSixLastFour(null));
  }

  @Test
  void testMaskPanShowFirstSixLastFour_emptyPan() {
    assertEquals("", PanMasker.maskPanShowFirstSixLastFour(""));
  }
}
