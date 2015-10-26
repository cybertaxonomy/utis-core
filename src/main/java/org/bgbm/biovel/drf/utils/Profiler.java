// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.bgbm.biovel.drf.utils;

import java.io.IOException;
import java.io.PrintStream;

import com.yourkit.api.Controller;

/**
 * @author a.kohlbecker
 * @date Oct 22, 2015
 *
 */
public class Profiler {

    private Controller yourkit;
    private boolean yjp = false;
    private boolean cpu = false;
    private long start;

    private Profiler() {

    }

    public static Profiler newCpuProfiler(boolean yjp) {
        Profiler p = new Profiler();
        p.yjp = yjp;
        p.cpu = true;
        if(yjp) {
            // https://www.yourkit.com/docs/java/api/com/yourkit/api/Controller.html
            try {
                p.yourkit = new Controller();
                p.yourkit.enableStackTelemetry();
                p.yourkit.startCPUTracing(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        p.start = System.currentTimeMillis();

        return p;
    }

    public void end(PrintStream err) {

        Long time = System.currentTimeMillis() - start;

        try {
            err.append(time.toString()).append(" ms\n");
            if(yjp) {
                if(cpu) {
                    yourkit.stopCPUProfiling();
                    String fileLocation = yourkit.captureSnapshot(com.yourkit.api.Controller.SNAPSHOT_WITHOUT_HEAP);
                    err.append("Snapshot stored at: ").append(fileLocation).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
