package org.bgbm.biovel.drf.checklist;


public class DRFChecklistException extends Exception {

    public DRFChecklistException(Throwable cause) {
        super(cause);
    }

    public DRFChecklistException(String message) {
        super(message);
    }

    public DRFChecklistException(String message, Throwable cause) {
       super(message , cause);
    }

}
