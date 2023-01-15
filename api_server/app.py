import sys
import json
from revChatGPT.ChatGPT import Chatbot
from flask import Flask, request, abort, send_file

#chatbot = Chatbot({"session_token": sys.argv[1]})

app = Flask(__name__)

@app.get("/chatgpt")
def get_chatgpt_response():
	sound_name = request.args.get("sound_name")
	prompt = f'generate a single sentence response to the prompt "what would a Minecraft villager say if they heard {sound_name}"'
	#response = chatbot.ask(prompt)

	#return response["message"]
	return prompt


if __name__ == '__main__':
	app.run(host='0.0.0.0')