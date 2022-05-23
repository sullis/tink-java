// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.crypto.tink.signature;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.Registry;
import java.security.GeneralSecurityException;

/**
 * Deprecated class to create {@code PublicKeyVerify} primitives. Instead of using this class, make
 * sure that the {@code PublicKeyVerifyWrapper} is registered in your binary, then call {@code
 * keysetHandle.GetPrimitive(PublicKeyVerify.class)} instead. The required registration happens
 * automatically if you called one of the following in your binary:
 *
 * <ul>
 *   <li>{@code SignatureConfig.register()}
 *   <li>{@code TinkConfig.register()}
 * </ul>
 *
 * @deprecated Use {@code keysetHandle.GetPrimitive(PublicKeyVerify.class)} after registering the
 *     {@code PublicKeyVerifyWrapper} instead.
 * @since 1.0.0
 */
@Deprecated
public final class PublicKeyVerifyFactory {
  /**
   * @return a PublicKeyVerify primitive from a {@code keysetHandle}.
   * @throws GeneralSecurityException
   * @deprecated Use {@code keysetHandle.GetPrimitive(PublicKeyVerify.class)} after registering the
   *     {@code PublicKeyVerifyWrapper} instead.
   */
  @Deprecated
  public static PublicKeyVerify getPrimitive(KeysetHandle keysetHandle)
      throws GeneralSecurityException {
    Registry.registerPrimitiveWrapper(new PublicKeyVerifyWrapper());
    return keysetHandle.getPrimitive(PublicKeyVerify.class);
  }

  private PublicKeyVerifyFactory() {}
}
