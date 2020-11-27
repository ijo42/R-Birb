package ru.ijo42.rbirb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.token.Token;

import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tokens")
public class TokenModel extends BaseEntity implements Token {

    private String token;

    private String extendedInformation;

    @Override
    @JsonIgnore
    public String getKey() {
        return token;
    }

    @Override
    @JsonIgnore
    public long getKeyCreationTime() {
        return getCreated().getTime();
    }

    @Override
    @JsonIgnore
    public String getExtendedInformation() {
        return extendedInformation;
    }

    public TokenModel setStatusStreamed(Status status) {
        this.setStatus(status);
        return this;
    }
}
