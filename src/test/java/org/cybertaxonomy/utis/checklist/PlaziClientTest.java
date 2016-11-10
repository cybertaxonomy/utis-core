package org.cybertaxonomy.utis.checklist;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaziClientTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziClientTest.class);

    static PlaziClient client;

    @BeforeClass
    public static void  setup() {
        client =  new PlaziClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());
    }

    @Test
    public void client_init_Test() {
        assert(true);
    }

}

