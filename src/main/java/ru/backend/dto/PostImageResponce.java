
package ru.backend.dto;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "status",
    "success",
    "data"
})
@Generated("jsonschema2pojo")
public class PostImageResponce {

    @JsonProperty("status")
    private Integer status;
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("data")
    private Data data;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

//    @JsonProperty("status")
//    public Integer getStatus() {
//        return status;
//    }
//
//    @JsonProperty("status")
//    public void setStatus(Integer status) {
//        this.status = status;
//    }
//
//    public PostImageResponce withStatus(Integer status) {
//        this.status = status;
//        return this;
//    }
//
//    @JsonProperty("success")
//    public Boolean getSuccess() {
//        return success;
//    }
//
//    @JsonProperty("success")
//    public void setSuccess(Boolean success) {
//        this.success = success;
//    }
//
//    public PostImageResponce withSuccess(Boolean success) {
//        this.success = success;
//        return this;
//    }
//
//    @JsonProperty("data")
//    public Data getData() {
//        return data;
//    }
//
//    @JsonProperty("data")
//    public void setData(Data data) {
//        this.data = data;
//    }
//
//    public PostImageResponce withData(Data data) {
//        this.data = data;
//        return this;
//    }
//
//    @JsonAnyGetter
//    public Map<String, Object> getAdditionalProperties() {
//        return this.additionalProperties;
//    }
//
//    @JsonAnySetter
//    public void setAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//    }
//
//    public PostImageResponce withAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//        return this;
//    }

}
