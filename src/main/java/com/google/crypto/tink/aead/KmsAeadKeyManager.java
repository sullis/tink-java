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

package com.google.crypto.tink.aead;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KmsClient;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.internal.KeyTypeManager;
import com.google.crypto.tink.internal.PrimitiveFactory;
import com.google.crypto.tink.proto.KeyData.KeyMaterialType;
import com.google.crypto.tink.proto.KmsAeadKey;
import com.google.crypto.tink.proto.KmsAeadKeyFormat;
import com.google.crypto.tink.subtle.Validators;
import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.GeneralSecurityException;

/**
 * This key manager produces new instances of {@code Aead} that forwards encrypt/decrypt requests to
 * a key residing in a remote KMS.
 */
public class KmsAeadKeyManager extends KeyTypeManager<KmsAeadKey> {
  KmsAeadKeyManager() {
    super(
        KmsAeadKey.class,
        new PrimitiveFactory<Aead, KmsAeadKey>(Aead.class) {
          @Override
          public Aead getPrimitive(KmsAeadKey keyProto) throws GeneralSecurityException {
            String keyUri = keyProto.getParams().getKeyUri();
            KmsClient kmsClient = KmsClients.get(keyUri);
            return kmsClient.getAead(keyUri);
          }
        });
  }

  @Override
  public String getKeyType() {
    return "type.googleapis.com/google.crypto.tink.KmsAeadKey";
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public KeyMaterialType keyMaterialType() {
    return KeyMaterialType.REMOTE;
  }

  @Override
  public void validateKey(KmsAeadKey key) throws GeneralSecurityException {
    Validators.validateVersion(key.getVersion(), getVersion());
  }

  @Override
  public KmsAeadKey parseKey(ByteString byteString) throws InvalidProtocolBufferException {
    return KmsAeadKey.parseFrom(byteString, ExtensionRegistryLite.getEmptyRegistry());
  }

  @Override
  public KeyFactory<KmsAeadKeyFormat, KmsAeadKey> keyFactory() {
    return new KeyFactory<KmsAeadKeyFormat, KmsAeadKey>(KmsAeadKeyFormat.class) {
      @Override
      public void validateKeyFormat(KmsAeadKeyFormat format) throws GeneralSecurityException {}

      @Override
      public KmsAeadKeyFormat parseKeyFormat(ByteString byteString)
          throws InvalidProtocolBufferException {
        return KmsAeadKeyFormat.parseFrom(byteString, ExtensionRegistryLite.getEmptyRegistry());
      }

      @Override
      public KmsAeadKey createKey(KmsAeadKeyFormat format) throws GeneralSecurityException {
        return KmsAeadKey.newBuilder().setParams(format).setVersion(getVersion()).build();
      }
    };
  }

  public static void register(boolean newKeyAllowed) throws GeneralSecurityException {
    Registry.registerKeyManager(new KmsAeadKeyManager(), newKeyAllowed);
  }

  /**
   * Returns a new {@link KeyTemplate} that can generate a {@link
   * com.google.crypto.tink.proto.KmsAeadKey} whose key encrypting key (KEK) is pointing to {@code
   * kekUri}. Keys generated by this key template uses RAW output prefix to make them compatible
   * with the remote KMS' encrypt/decrypt operations. Unlike other templates, when you call {@link
   * KeysetHandle#generateNew} with this template, Tink does not generate new key material, but only
   * creates a reference to the remote KEK.
   */
  public static KeyTemplate createKeyTemplate(String kekUri) {
    KmsAeadKeyFormat format = createKeyFormat(kekUri);
    return KeyTemplate.create(
        new KmsAeadKeyManager().getKeyType(),
        format.toByteArray(),
        KeyTemplate.OutputPrefixType.RAW);
  }

  static KmsAeadKeyFormat createKeyFormat(String keyUri) {
    return KmsAeadKeyFormat.newBuilder().setKeyUri(keyUri).build();
  }
}
