/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.inference.action;

import org.elasticsearch.TransportVersions;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.inference.ModelConfigurations;
import org.elasticsearch.inference.TaskType;
import org.elasticsearch.xcontent.ToXContentObject;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GetInferenceModelAction extends ActionType<GetInferenceModelAction.Response> {

    public static final GetInferenceModelAction INSTANCE = new GetInferenceModelAction();
    public static final String NAME = "cluster:admin/xpack/inference/get";

    public GetInferenceModelAction() {
        super(NAME, GetInferenceModelAction.Response::new);
    }

    public static class Request extends AcknowledgedRequest<GetInferenceModelAction.Request> {

        private final String modelId;
        private final TaskType taskType;

        public Request(String modelId, String taskType) {
            this.modelId = modelId;
            this.taskType = TaskType.fromStringOrStatusException(taskType);
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            this.modelId = in.readString();
            this.taskType = TaskType.fromStream(in);
        }

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        public String getModelId() {
            return modelId;
        }

        public TaskType getTaskType() {
            return taskType;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(modelId);
            taskType.writeTo(out);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Request request = (Request) o;
            return Objects.equals(modelId, request.modelId) && taskType == request.taskType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(modelId, taskType);
        }
    }

    public static class Response extends ActionResponse implements ToXContentObject {

        private final List<ModelConfigurations> models;

        public Response(List<ModelConfigurations> models) {
            this.models = models;
        }

        public Response(StreamInput in) throws IOException {
            super(in);
            if (in.getTransportVersion().onOrAfter(TransportVersions.ML_INFERENCE_GET_MULTIPLE_MODELS)) {
                models = in.readCollectionAsList(ModelConfigurations::new);
            } else {
                models = new ArrayList<>();
                models.add(new ModelConfigurations(in));
            }
        }

        public List<ModelConfigurations> getModels() {
            return models;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            if (out.getTransportVersion().onOrAfter(TransportVersions.ML_INFERENCE_GET_MULTIPLE_MODELS)) {
                out.writeCollection(models);
            } else {
                models.get(0).writeTo(out);
            }
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.startArray("models");
            for (var model : models) {
                if (model != null) {
                    model.toXContent(builder, params);
                }
            }
            builder.endArray();
            builder.endObject();
            return builder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GetInferenceModelAction.Response response = (GetInferenceModelAction.Response) o;
            return Objects.equals(models, response.models);
        }

        @Override
        public int hashCode() {
            return Objects.hash(models);
        }
    }
}
