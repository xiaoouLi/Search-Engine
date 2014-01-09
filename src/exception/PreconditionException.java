// Xiaoou, Dec,2013
// xli65@usfca.edu
package exception;

/**
 * @author xli65
 */
@SuppressWarnings("serial")
public class PreconditionException extends RuntimeException {

    public PreconditionException(String message) {
        super(message);
    }

    public PreconditionException(Exception e) {
        super(e);
    }
}
