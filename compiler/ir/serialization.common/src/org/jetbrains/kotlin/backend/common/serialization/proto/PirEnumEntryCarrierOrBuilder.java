// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

public interface PirEnumEntryCarrierOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.backend.common.serialization.proto.PirEnumEntryCarrier)
    org.jetbrains.kotlin.protobuf.MessageLiteOrBuilder {

  /**
   * <code>required int32 lastModified = 1;</code>
   */
  boolean hasLastModified();
  /**
   * <code>required int32 lastModified = 1;</code>
   */
  int getLastModified();

  /**
   * <code>optional int64 parentSymbol = 2;</code>
   */
  boolean hasParentSymbol();
  /**
   * <code>optional int64 parentSymbol = 2;</code>
   */
  long getParentSymbol();

  /**
   * <code>optional int32 origin = 3;</code>
   */
  boolean hasOrigin();
  /**
   * <code>optional int32 origin = 3;</code>
   */
  int getOrigin();

  /**
   * <code>optional int64 correspondingClass = 4;</code>
   */
  boolean hasCorrespondingClass();
  /**
   * <code>optional int64 correspondingClass = 4;</code>
   */
  long getCorrespondingClass();

  /**
   * <code>optional int32 initializerExpression = 5;</code>
   */
  boolean hasInitializerExpression();
  /**
   * <code>optional int32 initializerExpression = 5;</code>
   */
  int getInitializerExpression();
}