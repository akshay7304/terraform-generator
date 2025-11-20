package com.example.tfgenerator.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CidrUtilTest {

    @Test
    public void testSplitCidrIntoTwoSubnets() {
        String cidr = "10.0.0.0/16";
        List<String> subnets = CidrUtil.splitCidr(cidr, 2);
        assertEquals(2, subnets.size());
        assertEquals("10.0.0.0/17", subnets.get(0));
        assertEquals("10.0.128.0/17", subnets.get(1));
    }
}
