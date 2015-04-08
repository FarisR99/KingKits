package com.faris.kingkits.helper;

public class Time {
	private int hours, minutes, seconds;
	private int total;

	public Time(int hours, int minutes, int seconds) {
		this.total = seconds + (minutes * 60) + (hours * 3600);
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	public Time(int inputSeconds) {
		this.total = inputSeconds;
		this.hours = inputSeconds / 3600;
		int remainder = inputSeconds % 3600;
		this.minutes = remainder / 60;
		this.seconds = remainder % 60;
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
