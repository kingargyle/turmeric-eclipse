/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.turmeric.eclipse.registry.exception;

/**
 * indicates problems occured during client provider interactions.
 * @author yayu
 * @since 1.0.0
 */
public class ClientProviderException extends ProviderException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 240L;

	/**
	 * Instantiates a new client provider exception.
	 */
	public ClientProviderException() {
		super();
	}

	/**
	 * Instantiates a new client provider exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public ClientProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new client provider exception.
	 *
	 * @param message the message
	 */
	public ClientProviderException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new client provider exception.
	 *
	 * @param cause the cause
	 */
	public ClientProviderException(Throwable cause) {
		super(cause);
	}
	
	

}
