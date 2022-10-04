FROM node
WORKDIR /app
COPY package.json /app
RUN npm i
COPY * /app/
USER 1000
EXPOSE 3000
CMD [ "node","index.js" ]
