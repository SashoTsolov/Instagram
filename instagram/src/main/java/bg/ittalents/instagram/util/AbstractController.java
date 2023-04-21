package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

public abstract class AbstractController {

    protected long getLoggedId(HttpSession s) {
        if (s.getAttribute("LOGGED_ID") == null) {
            throw new UnauthorizedException("You have to login first");
        }
        return (long) s.getAttribute("LOGGED_ID");
    }


}
