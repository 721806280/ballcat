/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballcat.log.operation;

/**
 * 操作执行者，用于包装操作中的异常。参考 {@link org.springframework.cache.interceptor.CacheOperationInvoker}
 *
 * @author Hccake
 * @since 2.0.0
 */
@FunctionalInterface
public interface OperationInvoker {

	/**
	 * Invoke the operation defined by this instance. Wraps any exception that is thrown
	 * during the invocation in a {@link ThrowableWrapper}.
	 * @return the result of the operation
	 * @throws ThrowableWrapper if an error occurred while invoking the operation
	 */
	Object invoke() throws ThrowableWrapper;

	/**
	 * Wrap any exception thrown while invoking {@link #invoke()}.
	 */
	@SuppressWarnings("serial")
	class ThrowableWrapper extends RuntimeException {

		private final Throwable original;

		public ThrowableWrapper(Throwable original) {
			super(original.getMessage(), original);
			this.original = original;
		}

		public Throwable getOriginal() {
			return this.original;
		}

	}

}
