package com.kh.edu.istad.product.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class ProductGrpcServiceGrpc {

  private ProductGrpcServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "kh.edu.istad.product.grpc.ProductGrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.kh.edu.istad.product.grpc.ProductRequest,
      com.kh.edu.istad.product.grpc.ProductResponse> getGetProductByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProductById",
      requestType = com.kh.edu.istad.product.grpc.ProductRequest.class,
      responseType = com.kh.edu.istad.product.grpc.ProductResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.kh.edu.istad.product.grpc.ProductRequest,
      com.kh.edu.istad.product.grpc.ProductResponse> getGetProductByIdMethod() {
    io.grpc.MethodDescriptor<com.kh.edu.istad.product.grpc.ProductRequest, com.kh.edu.istad.product.grpc.ProductResponse> getGetProductByIdMethod;
    if ((getGetProductByIdMethod = ProductGrpcServiceGrpc.getGetProductByIdMethod) == null) {
      synchronized (ProductGrpcServiceGrpc.class) {
        if ((getGetProductByIdMethod = ProductGrpcServiceGrpc.getGetProductByIdMethod) == null) {
          ProductGrpcServiceGrpc.getGetProductByIdMethod = getGetProductByIdMethod =
              io.grpc.MethodDescriptor.<com.kh.edu.istad.product.grpc.ProductRequest, com.kh.edu.istad.product.grpc.ProductResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProductById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.kh.edu.istad.product.grpc.ProductRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.kh.edu.istad.product.grpc.ProductResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProductGrpcServiceMethodDescriptorSupplier("GetProductById"))
              .build();
        }
      }
    }
    return getGetProductByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.kh.edu.istad.product.grpc.CreateProductRequest,
      com.kh.edu.istad.product.grpc.ProductResponse> getCreateProductMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateProduct",
      requestType = com.kh.edu.istad.product.grpc.CreateProductRequest.class,
      responseType = com.kh.edu.istad.product.grpc.ProductResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.kh.edu.istad.product.grpc.CreateProductRequest,
      com.kh.edu.istad.product.grpc.ProductResponse> getCreateProductMethod() {
    io.grpc.MethodDescriptor<com.kh.edu.istad.product.grpc.CreateProductRequest, com.kh.edu.istad.product.grpc.ProductResponse> getCreateProductMethod;
    if ((getCreateProductMethod = ProductGrpcServiceGrpc.getCreateProductMethod) == null) {
      synchronized (ProductGrpcServiceGrpc.class) {
        if ((getCreateProductMethod = ProductGrpcServiceGrpc.getCreateProductMethod) == null) {
          ProductGrpcServiceGrpc.getCreateProductMethod = getCreateProductMethod =
              io.grpc.MethodDescriptor.<com.kh.edu.istad.product.grpc.CreateProductRequest, com.kh.edu.istad.product.grpc.ProductResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateProduct"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.kh.edu.istad.product.grpc.CreateProductRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.kh.edu.istad.product.grpc.ProductResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ProductGrpcServiceMethodDescriptorSupplier("CreateProduct"))
              .build();
        }
      }
    }
    return getCreateProductMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ProductGrpcServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceStub>() {
        @java.lang.Override
        public ProductGrpcServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProductGrpcServiceStub(channel, callOptions);
        }
      };
    return ProductGrpcServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static ProductGrpcServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceBlockingV2Stub>() {
        @java.lang.Override
        public ProductGrpcServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProductGrpcServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return ProductGrpcServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ProductGrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceBlockingStub>() {
        @java.lang.Override
        public ProductGrpcServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProductGrpcServiceBlockingStub(channel, callOptions);
        }
      };
    return ProductGrpcServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ProductGrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ProductGrpcServiceFutureStub>() {
        @java.lang.Override
        public ProductGrpcServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ProductGrpcServiceFutureStub(channel, callOptions);
        }
      };
    return ProductGrpcServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getProductById(com.kh.edu.istad.product.grpc.ProductRequest request,
        io.grpc.stub.StreamObserver<com.kh.edu.istad.product.grpc.ProductResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetProductByIdMethod(), responseObserver);
    }

    /**
     */
    default void createProduct(com.kh.edu.istad.product.grpc.CreateProductRequest request,
        io.grpc.stub.StreamObserver<com.kh.edu.istad.product.grpc.ProductResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateProductMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ProductGrpcService.
   */
  public static abstract class ProductGrpcServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ProductGrpcServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ProductGrpcService.
   */
  public static final class ProductGrpcServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ProductGrpcServiceStub> {
    private ProductGrpcServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProductGrpcServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProductGrpcServiceStub(channel, callOptions);
    }

    /**
     */
    public void getProductById(com.kh.edu.istad.product.grpc.ProductRequest request,
        io.grpc.stub.StreamObserver<com.kh.edu.istad.product.grpc.ProductResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetProductByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createProduct(com.kh.edu.istad.product.grpc.CreateProductRequest request,
        io.grpc.stub.StreamObserver<com.kh.edu.istad.product.grpc.ProductResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateProductMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ProductGrpcService.
   */
  public static final class ProductGrpcServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<ProductGrpcServiceBlockingV2Stub> {
    private ProductGrpcServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProductGrpcServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProductGrpcServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    public com.kh.edu.istad.product.grpc.ProductResponse getProductById(com.kh.edu.istad.product.grpc.ProductRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getGetProductByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.kh.edu.istad.product.grpc.ProductResponse createProduct(com.kh.edu.istad.product.grpc.CreateProductRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getCreateProductMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service ProductGrpcService.
   */
  public static final class ProductGrpcServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ProductGrpcServiceBlockingStub> {
    private ProductGrpcServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProductGrpcServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProductGrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.kh.edu.istad.product.grpc.ProductResponse getProductById(com.kh.edu.istad.product.grpc.ProductRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetProductByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.kh.edu.istad.product.grpc.ProductResponse createProduct(com.kh.edu.istad.product.grpc.CreateProductRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateProductMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ProductGrpcService.
   */
  public static final class ProductGrpcServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ProductGrpcServiceFutureStub> {
    private ProductGrpcServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProductGrpcServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ProductGrpcServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.kh.edu.istad.product.grpc.ProductResponse> getProductById(
        com.kh.edu.istad.product.grpc.ProductRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetProductByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.kh.edu.istad.product.grpc.ProductResponse> createProduct(
        com.kh.edu.istad.product.grpc.CreateProductRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateProductMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_PRODUCT_BY_ID = 0;
  private static final int METHODID_CREATE_PRODUCT = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_PRODUCT_BY_ID:
          serviceImpl.getProductById((com.kh.edu.istad.product.grpc.ProductRequest) request,
              (io.grpc.stub.StreamObserver<com.kh.edu.istad.product.grpc.ProductResponse>) responseObserver);
          break;
        case METHODID_CREATE_PRODUCT:
          serviceImpl.createProduct((com.kh.edu.istad.product.grpc.CreateProductRequest) request,
              (io.grpc.stub.StreamObserver<com.kh.edu.istad.product.grpc.ProductResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetProductByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.kh.edu.istad.product.grpc.ProductRequest,
              com.kh.edu.istad.product.grpc.ProductResponse>(
                service, METHODID_GET_PRODUCT_BY_ID)))
        .addMethod(
          getCreateProductMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.kh.edu.istad.product.grpc.CreateProductRequest,
              com.kh.edu.istad.product.grpc.ProductResponse>(
                service, METHODID_CREATE_PRODUCT)))
        .build();
  }

  private static abstract class ProductGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ProductGrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.kh.edu.istad.product.grpc.ProductProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ProductGrpcService");
    }
  }

  private static final class ProductGrpcServiceFileDescriptorSupplier
      extends ProductGrpcServiceBaseDescriptorSupplier {
    ProductGrpcServiceFileDescriptorSupplier() {}
  }

  private static final class ProductGrpcServiceMethodDescriptorSupplier
      extends ProductGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ProductGrpcServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ProductGrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ProductGrpcServiceFileDescriptorSupplier())
              .addMethod(getGetProductByIdMethod())
              .addMethod(getCreateProductMethod())
              .build();
        }
      }
    }
    return result;
  }
}
