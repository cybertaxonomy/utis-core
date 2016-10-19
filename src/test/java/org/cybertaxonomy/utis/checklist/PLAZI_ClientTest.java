package org.cybertaxonomy.utis.checklist;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PLAZI_ClientTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PLAZI_ClientTest.class);

    static PLAZIClient client;

    @BeforeClass
    public static void  setup() {
        client =  new PLAZIClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());
    }

    @Test
    public void client_init_Test() {
        assert(true);
    }

}

