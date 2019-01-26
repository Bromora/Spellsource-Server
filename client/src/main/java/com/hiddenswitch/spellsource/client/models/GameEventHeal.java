/*
 * Hidden Switch Spellsource API
 * The Spellsource API for matchmaking, user accounts, collections management and more.  To get started, create a user account and make sure to include the entirety of the returned login token as the X-Auth-Token header. You can reuse this token, or login for a new one.  ClientToServerMessage and ServerToClientMessage are used for the realtime game state and actions two-way websocket interface for actually playing a game. Envelope is used for the realtime API services.  For the routes that correspond to the paths in this file, visit the Gateway.java file in the Spellsource-Server GitHub repository located in this definition file. 
 *
 * OpenAPI spec version: 3.0.2
 * Contact: ben@hiddenswitch.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.hiddenswitch.spellsource.client.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hiddenswitch.spellsource.client.models.Entity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * GameEventHeal
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)

public class GameEventHeal implements Serializable {
  private static final long serialVersionUID = 1L;

  @JsonProperty("target")
  private Entity target = null;

  @JsonProperty("healing")
  private Integer healing = null;

  public GameEventHeal target(Entity target) {
    this.target = target;
    return this;
  }

   /**
   * Get target
   * @return target
  **/
  @ApiModelProperty(value = "")
  public Entity getTarget() {
    return target;
  }

  public void setTarget(Entity target) {
    this.target = target;
  }

  public GameEventHeal healing(Integer healing) {
    this.healing = healing;
    return this;
  }

   /**
   * Get healing
   * @return healing
  **/
  @ApiModelProperty(value = "")
  public Integer getHealing() {
    return healing;
  }

  public void setHealing(Integer healing) {
    this.healing = healing;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GameEventHeal gameEventHeal = (GameEventHeal) o;
    return Objects.equals(this.target, gameEventHeal.target) &&
        Objects.equals(this.healing, gameEventHeal.healing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target, healing);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GameEventHeal {\n");
    
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    healing: ").append(toIndentedString(healing)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

