package events;

import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.addon.globalevents.GlobalUiEvent;

public class DashboardInvalidateEvent extends GlobalApplicationEvent implements GlobalUiEvent {


    private final String message;

    public DashboardInvalidateEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }



}
