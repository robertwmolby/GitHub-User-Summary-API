package api.molby.githubSummary.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Class to handle exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GitHubApiAccessException.class)
    public ProblemDetail handleGitHubApiAccessException(GitHubApiAccessException e) {
        log.error("Error accessing GitHub API for user {}.",e.getUserName(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Error accessing GitHub API");
        problemDetail.setDetail(e.getMessage());
        problemDetail.setProperty("userName", e.getUserName());
        return problemDetail;
    }

    @ExceptionHandler(GitHubUserNotFoundException.class)
    public ProblemDetail handleGitHubUserNotFoundException(GitHubUserNotFoundException e) {
        log.warn("Request was made with user that was not found in GitHub.  userrName: {}.", e.getUserName());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("User not found in GitHub");
        problemDetail.setDetail(e.getMessage());
        problemDetail.setProperty("userName", e.getUserName());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(ConstraintViolationException e, HttpServletRequest request) {
        log.info("Request was made with invalid parameters.  message: {}.", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Failed.");
        problemDetail.setDetail("Information provided contained invalid values.");
        problemDetail.setProperty("uri",request.getRequestURI());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.info("Request was made with invalid parameters.  message: {}.", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Request parameters were invalid.");
        problemDetail.setDetail(e.getMessage());
        problemDetail.setProperty("uri",request.getRequestURI());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("Unexpected exception occurred while handling request.", e);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Unexpected exception occurred while handling request.");
        problemDetail.setDetail(e.getMessage());
        return problemDetail;
    }
}
