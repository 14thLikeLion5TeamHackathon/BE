package likelion.madi.common.oauth2;

import java.io.Serializable;

import likelion.madi.domain.User;
import lombok.Getter;

@Getter
public class UserDTO implements Serializable {
    private Long userId;
    private String name;

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
    }
}
