/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.simple;

import org.jspecify.annotations.Nullable;

public class AddressRecord {
    private @Nullable Integer id;
    private @Nullable String streetAddress;
    private @Nullable String city;
    private @Nullable String state;
    private @Nullable AddressType addressType;

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public @Nullable String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public @Nullable String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public @Nullable String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public @Nullable AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public enum AddressType {
        HOME,
        BUSINESS
    }
}
