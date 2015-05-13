package eu.emrex.client.exception;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * 
 * @author leivhe
 * 
 */
public class ApplicationExceptionHandlerFactory extends ExceptionHandlerFactory {

    private ExceptionHandlerFactory parent;


    public ApplicationExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }


    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler result = parent.getExceptionHandler();
        result = new ApplicationExceptionHandler(result);

        return result;
    }

}
