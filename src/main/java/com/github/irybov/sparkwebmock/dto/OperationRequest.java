package com.github.irybov.sparkwebmock.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OperationRequest {

	@SerializedName("sender")
	@NotNull(message = "Sender must not be null")
	@Max(value = 999_999_999, message = "Sender's bill number should be less than 10 digits length")
	@Positive(message = "Sender's bill number should be positive number")
	private Integer sender;
	
	@SerializedName("recipient")
	@NotNull(message = "Recepient must not be null")
	@Max(value = 999_999_999, message = "Recepient's bill number should be less than 10 digits length")
	@Positive(message = "Recepient's bill number should be positive number")
	private Integer recipient;
	
	@SerializedName("currency")
	@NotBlank(message = "Currency must not be blank")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Currency code should be 3 capital characters length")
	private String currency;
	
	@SerializedName("amount")
	@NotNull(message = "Amount must not be null")
	@Positive(message = "Amount of money should be higher than zero")
	private Double amount;
	
	private String bank;
	
}
