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

package org.ballcat.common.value.cycle;

import java.util.Iterator;

import org.ballcat.common.value.step.IteratorStepValue;

/**
 * @author lingting 2024-01-23 15:24
 */
public class IteratorCycleValue<T> extends AbstractCycleValue<T> {

	private final IteratorStepValue<T> step;

	public IteratorCycleValue(Iterator<T> iterator) {
		this(new IteratorStepValue<>(iterator));
	}

	public IteratorCycleValue(IteratorStepValue<T> step) {
		this.step = step;
	}

	@Override
	public void reset() {
		this.step.reset();
	}

	@Override
	public T doNext() {
		if (!this.step.hasNext()) {
			this.step.reset();
		}
		return this.step.next();
	}

	/**
	 * 移除上一个next返回的元素
	 */
	public void remove() {
		this.step.remove();
	}

}
