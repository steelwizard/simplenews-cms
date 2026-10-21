package simplenews.cms.data.dto;

import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.data.entity.ReviewType;

import java.math.BigDecimal;
import java.util.List;

public record ReviewTypeJsonDto(String key, String title, List<Field> fields) {

	public record Field(String key, String label, ReviewFieldTypeEnum type, boolean required,
						BigDecimal min, BigDecimal max, List<String> choices, boolean filterable) {
		public Field(ReviewField field) {
			this(field.getKey(), field.getLabel(), field.getFieldType(), field.isRequired(),
					field.getMinValue(), field.getMaxValue(),
					field.getFieldType() == ReviewFieldTypeEnum.CHOICE ? field.getChoiceList() : null,
					field.isFilterable());
		}
	}

	public ReviewTypeJsonDto(ReviewType type, List<ReviewField> fields) {
		this(type.getKey(), type.getTitle(), fields.stream().map(Field::new).toList());
	}
}
