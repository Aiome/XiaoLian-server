package top.aiome.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseCollegeComment<M extends BaseCollegeComment<M>> extends Model<M> implements IBean {

	public void setCommentId(java.lang.String commentId) {
		set("commentId", commentId);
	}

	public java.lang.String getCommentId() {
		return get("commentId");
	}

	public void setSchoolId(java.lang.String schoolId) {
		set("schoolId", schoolId);
	}

	public java.lang.String getSchoolId() {
		return get("schoolId");
	}

	public void setComment(java.lang.String comment) {
		set("comment", comment);
	}

	public java.lang.String getComment() {
		return get("comment");
	}

	public void setScore(java.lang.String score) {
		set("score", score);
	}

	public java.lang.String getScore() {
		return get("score");
	}

	public void setImage(java.lang.String image) {
		set("image", image);
	}

	public java.lang.String getImage() {
		return get("image");
	}

	public void setUserId(java.lang.String userId) {
		set("userId", userId);
	}

	public java.lang.String getUserId() {
		return get("userId");
	}

}
