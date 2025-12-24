package simplenews.cms.security;

public enum RoleEnum {
	GUEST(Names.GUEST, 10),
	REGISTERED(Names.REGISTERED, 20),
	USER(Names.USER, 30),
	EDITOR(Names.EDITOR, 40),
	ADMIN(Names.ADMIN, 50);

	RoleEnum(String label, int order) {
		this.label = label;
		this.order = order;
	}

	public class Names {
		public static final String GUEST		= "GUEST";
		public static final String REGISTERED	= "REGISTERED";
		public static final String USER			= "USER";
		public static final String EDITOR		= "EDITOR";
		public static final String ADMIN		= "ADMIN";
	}


	private final String label;
	private final int order;

	public String toString() {
		return this.label;
	}

	public String getLabel() {
		return label;
	}

	public int getOrder() {
		return order;
	}

}
