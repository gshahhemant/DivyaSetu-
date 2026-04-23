package com.swadhyaydata.app.entity;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Comments {
	
	@JsonProperty("note")
	private String note;
	@JsonProperty("year")
	private String year;
	@JsonProperty("month")
	private String month;
	
	
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}

		
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comments)) return false;
        Comments that = (Comments) o;
        return Objects.equals(note, that.note) &&
               Objects.equals(year, that.year) &&
               Objects.equals(month, that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, year, month);
    }

    @Override
    public String toString() {
        return "Comments{" +
                "note='" + note + '\'' +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                '}';
    }


}
