import bcrypt
import sys

input = "admin"
if (len(sys.argv)>1):
    input = sys.argv[1]
    bytes = input.encode('utf-8')
    print(bcrypt.hashpw(bytes, bcrypt.gensalt(rounds=10)).decode("utf-8"))
else:
    print("bcrypt-encode [STRING]")
