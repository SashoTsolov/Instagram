package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public abstract class AbstractController {

    protected final HttpServletRequest request;
    protected final HttpSession session;

    public AbstractController(final HttpServletRequest request, final HttpSession session) {
        this.request = request;
        this.session = session;
    }

    protected long getLoggedId() {
        final String USER_IP = request.getRemoteAddr();

        if (session.getAttribute("LOGGED_ID") == null || !session.getAttribute("IP").equals(USER_IP)) {
            throw new UnauthorizedException("You have to login first");
        }
        return (long) session.getAttribute("LOGGED_ID");
    }
}
