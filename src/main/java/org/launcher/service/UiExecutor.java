package org.launcher.service;

import org.launcher.exception.BaseException;
import org.launcher.exception.Exceptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiExecutor {
    private static final Logger logger = LoggerFactory.getLogger(UiExecutor.class);

    private UiExecutor() {
    }

    public static void execute(Exceptional action) {
        try {
            action.run();
        } catch (BaseException e) {
            if(e.isVisible()) {
                NotificationService.show(e);
            }else{
                logger.error(e.toString());
            }
        } catch (Exception e) {
            logger.error("Unexpected error", e);
        }
    }
}
