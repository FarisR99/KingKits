package com.faris.kingkits.helper;

import java.util.concurrent.*;

public class Time {

	private int days;
	private int hours, minutes, seconds;
	private int total;

	public Time(int inputSeconds) {
		this.total = inputSeconds;
		this.days = (int) TimeUnit.SECONDS.toDays(inputSeconds);
		this.hours = (int) (TimeUnit.SECONDS.toHours(inputSeconds) - (this.days * 24));
		this.minutes = (int) (TimeUnit.SECONDS.toMinutes(inputSeconds) - (TimeUnit.SECONDS.toHours(inputSeconds) * 60));
		this.seconds = (int) (TimeUnit.SECONDS.toSeconds(inputSeconds) - (TimeUnit.SECONDS.toMinutes(inputSeconds) * 60));
	}

	public int getDays() {
		return this.days;
	}

	public int getHours() {
		return this.hours;
	}

	public int getMinutes() {
		return this.minutes;
	}

	public int getSeconds() {
		return this.seconds;
	}

	public int getTotal() {
		return this.total;
	}

}