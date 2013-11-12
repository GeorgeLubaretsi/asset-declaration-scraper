# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html

class DeclarationPipeline(object):
    def process_item(self, item, spider):
        path = item['url']
        if item['lang'] == 'en':
            with open("./output/en/"+item['pdfid']+".pdf", "wb") as f:
                f.write(item['body'])
        else:
            with open("./output/ka/"+item['pdfid']+".pdf", "wb") as f:
                f.write(item['body'])
        # remove body and add path as reference
        del item['body']

        return item


