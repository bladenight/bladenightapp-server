package de.greencity.bladenightapp.server;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.tasks.ComputeSchedulerClient;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RouteUpdater implements Runnable {
    private long period;
    private Procession procession;
    private EventList eventList;
    private RouteStore routeStore;
    private Route lastRoute;
    private Log log;

    public RouteUpdater(Procession procession, RouteStore routeStore, EventList eventList, long period) {
        this.procession = procession;
        this.routeStore = routeStore;
        this.eventList = eventList;
        this.period = period;
    }

    @Override
    public void run() {
        boolean cont = true;
        while (cont) {
            update();
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
                cont = false;
            }
        }
    }

    void update() {
        Route currentRoute = procession.getRoute();

        Event nextEvent = eventList.getNextEvent();
        if (nextEvent != null) {
            if(currentRoute == null || !currentRoute.getName().equals(nextEvent.getRouteName())) {
                Route route = routeStore.getRoute(nextEvent.getRouteName());
                if ( route == null ) {
                    getLog().error("Route for next event unknown: " + nextEvent.getRouteName());
                }
                else {
                    getLog().info("Active route changed to " + route.getName());
                    procession.setRoute(route);
                }
            }
        } else {
            getLog().warn("No upcoming event found");
            procession.setRoute(null);
        }

    }

    private Log getLog() {
        if (log == null)
            log = LogFactory.getLog(RouteUpdater.class);
        return log;
    }

}

